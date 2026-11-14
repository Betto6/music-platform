package com.soundapp.music.core.render;

import com.soundapp.music.core.engine.AudioContext;
import com.soundapp.music.core.engine.AudioSource;
import com.soundapp.music.core.engine.ParameterDescribable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Trasforma un oscillatore in un file WAV su disco.
 *
 * È il gemello "offline" di AudioPreview: stessa logica di generazione e
 * conversione dei sample, ma invece di spingerli in tempo reale sulla scheda
 * audio li accumula tutti in un buffer e li scrive in un colpo solo su file,
 * tramite AudioSystem.write(). Usiamo javax.sound.sampled anche qui (invece di
 * scrivere a mano l'header RIFF/WAVE) perché la libreria standard di Java sa
 * già come produrre un WAV valido a partire da un AudioInputStream: ci
 * risparmia di reimplementare a mano il formato del file.
 *
 * Accanto al WAV scrive anche un file "sidecar" con lo stesso basename ed
 * estensione .yml, contenente i parametri con cui quel suono è stato generato -
 * a patto che la source sappia descriverli (vedi {@link ParameterDescribable}).
 * Serve a poter riaprire un export tra mesi e sapere esattamente come
 * riprodurlo.
 */
public class WavRenderer {

    private static final Logger log = LoggerFactory.getLogger(WavRenderer.class);

    // Numero di FRAME (non byte, non singoli sample) generati per ogni chiamata
    // a oscillator.render(). Non è strettamente necessario "spezzare" la
    // generazione in chunk per un renderer offline (potremmo generare tutto in
    // un colpo solo), ma teniamo la stessa dimensione di AudioPreview per
    // coerenza e per non allocare array enormi con durate molto lunghe.
    private static final int FRAMES_PER_CHUNK = 512;

    // 16 bit = 2 byte per singolo sample (un canale, un istante).
    private static final int BYTES_PER_SAMPLE = 2;

    private static final String PARAMETER_DUMP_EXTENSION = ".yml";

    private final ParameterYamlWriter yamlWriter = new ParameterYamlWriter();

    public void render(AudioSource source, float durationSeconds, Path outputPath) {
        AudioFormat format = new AudioFormat(
                AudioContext.SAMPLE_RATE,
                AudioContext.SAMPLE_SIZE_IN_BITS,
                AudioContext.CHANNELS,
                true,   // signed: i sample PCM hanno segno (-32768..32767)
                false); // bigEndian = false: little-endian, lo standard nei file WAV

        int totalFrames = (int) (AudioContext.SAMPLE_RATE * durationSeconds);

        // A differenza di AudioPreview (che scrive man mano sulla SourceDataLine),
        // qui dobbiamo avere TUTTI i byte pronti prima di scrivere il file, perché
        // AudioInputStream/AudioSystem.write hanno bisogno di conoscere in anticipo
        // la lunghezza totale in frame per generare un header WAV corretto.
        byte[] pcmData = new byte[totalFrames * AudioContext.CHANNELS * BYTES_PER_SAMPLE];

        float[] sampleBuffer = new float[FRAMES_PER_CHUNK * AudioContext.CHANNELS];
        byte[] chunkBytes = new byte[sampleBuffer.length * BYTES_PER_SAMPLE];

        int framesRendered = 0;
        int writeOffset = 0;

        while (framesRendered < totalFrames) {
            source.render(sampleBuffer);
            toPcm16Bytes(sampleBuffer, chunkBytes);

            // L'ultimo giro del ciclo può "sforare": se mancano meno di
            // FRAMES_PER_CHUNK frame per arrivare a totalFrames, chunkBytes
            // contiene comunque un chunk pieno (oscillator.render riempie
            // sempre l'intero buffer che gli passiamo). Copiamo quindi solo i
            // byte che ci servono davvero, altrimenti scriveremmo oltre la
            // fine di pcmData (ArrayIndexOutOfBounds) o sporcheremmo il file
            // con sample "in più" mai contati in totalFrames.
            int framesRemaining = totalFrames - framesRendered;
            int framesToCopy = Math.min(FRAMES_PER_CHUNK, framesRemaining);
            int bytesToCopy = framesToCopy * AudioContext.CHANNELS * BYTES_PER_SAMPLE;

            System.arraycopy(chunkBytes, 0, pcmData, writeOffset, bytesToCopy);

            writeOffset += bytesToCopy;
            framesRendered += framesToCopy;
        }

        // AudioInputStream vuole la lunghezza in FRAME (totalFrames), non in byte:
        // è lei che si occupa di moltiplicare per canali e byte-per-sample usando
        // il "format" che le abbiamo passato.
        try (AudioInputStream audioInputStream =
                     new AudioInputStream(new ByteArrayInputStream(pcmData), format, totalFrames)) {
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputPath.toFile());
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile scrivere il file WAV: " + outputPath, e);
        }

        writeParameterDump(source, durationSeconds, outputPath);
    }

    /**
     * Scrive il dump dei parametri accanto al WAV appena esportato. Se la
     * source non sa descriversi non fa nulla, e non è un errore: è solo un
     * componente che non ha (ancora) parametri da dichiarare.
     *
     * Qualsiasi problema qui viene loggato e fermato, mai propagato: il WAV è
     * il deliverable e a questo punto è già su disco, mentre il dump è un
     * extra. Far fallire un export riuscito per un file di documentazione
     * sarebbe il peggiore dei due esiti possibili.
     */
    private void writeParameterDump(AudioSource source, float durationSeconds, Path outputPath) {
        if (!(source instanceof ParameterDescribable describable)) {
            return;
        }

        Path dumpPath = dumpPathFor(outputPath);
        try {
            String header = "# Parametri con cui è stato generato " + outputPath.getFileName() + "."
                    + System.lineSeparator()
                    + "# Generato automaticamente da WavRenderer: documenta l'export,"
                    + System.lineSeparator()
                    + "# non viene mai riletto dal codice."
                    + System.lineSeparator();

            String yaml = yamlWriter.toYaml(dumpContent(describable, durationSeconds));

            Files.writeString(dumpPath, header + yaml, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("WAV esportato, ma non è stato possibile scrivere il dump dei parametri in {}: {}",
                    dumpPath, e.getMessage(), e);
        }
    }

    /**
     * Metadati del render (quando, quanto lungo, in che formato, da cosa
     * generato) seguiti dai parametri della source, tutti sullo stesso
     * livello. Tenerli piatti insieme, invece di annidare i parametri sotto
     * una chiave, è ciò che lascia agli envelope interni di un synth il solo
     * livello di annidamento che il writer gestisce.
     *
     * Nota: un componente che chiamasse un proprio parametro "source" o
     * "sampleRate" ne sovrascriverebbe il metadato. È un rischio accettato -
     * si vedrebbe a occhio rileggendo il file - e non vale un prefisso su ogni
     * chiave per evitarlo.
     */
    private Map<String, Object> dumpContent(ParameterDescribable describable, float durationSeconds) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("renderedAt", ZonedDateTime.now()
                .truncatedTo(ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        content.put("source", describable.getClass().getSimpleName());
        content.put("durationSeconds", durationSeconds);
        content.put("sampleRate", AudioContext.SAMPLE_RATE);
        content.put("channels", AudioContext.CHANNELS);
        content.putAll(describable.describeParameters());
        return content;
    }

    /**
     * Stesso percorso e stesso basename del WAV, con estensione .yml:
     * out/kick_dark.wav -> out/kick_dark.yml. Se il nome non ha estensione, la
     * si aggiunge e basta.
     */
    private Path dumpPathFor(Path outputPath) {
        String fileName = outputPath.getFileName().toString();
        int extensionStart = fileName.lastIndexOf('.');
        String baseName = extensionStart > 0 ? fileName.substring(0, extensionStart) : fileName;
        return outputPath.resolveSibling(baseName + PARAMETER_DUMP_EXTENSION);
    }

    // Stessa identica conversione float -> PCM 16-bit little-endian di
    // AudioPreview.toPcm16Bytes(). È duplicata volutamente: il progetto segue
    // la regola "non fare refactoring prima che il ciclo base funzioni", e con
    // solo due utilizzatori non vale ancora la pena di estrarre un'astrazione
    // condivisa. Se un terzo componente avrà bisogno della stessa conversione,
    // allora sarà il momento di spostarla in un posto comune (es. audio-engine).
    private void toPcm16Bytes(float[] samples, byte[] out) {
        for (int i = 0; i < samples.length; i++) {
            short pcm = (short) (AudioContext.clamp(samples[i], -1.0f, 1.0f) * Short.MAX_VALUE);
            out[i * 2] = (byte) (pcm & 0xFF);         // byte basso
            out[i * 2 + 1] = (byte) ((pcm >> 8) & 0xFF); // byte alto
        }
    }
}
