package com.soundapp.music.core;

import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.*;

//Gestisce l'I/O audio con il sistema operativo
//E' sostanzialmente l'interfaccia tra il nostro AudioEngine e Java Sound API

@Slf4j
public class AudioIOManager {

    // === COMPONENTI JAVA SOUND ===

    /** Linea di output audio */
    private SourceDataLine audioLine;

    /** Thread per l'audio callback */
    private Thread audioThread;

    /** Riferimento al nostro engine */
    //Dev'essere inserito in tutti i costruttori, sennò potrebbe poi non essere inizializzato perchè final
    private final AudioEngine audioEngine;

    /** Flag per controllare il loop audio */
    private volatile boolean shouldRun = false;

    /** Buffer per convertire float → byte */
    private byte[] byteBuffer;

    /** Buffer di lavoro in float */
    private float[] floatBuffer;


    public AudioIOManager(AudioEngine engine) {
        this.audioEngine = engine;

        // Inizializza i buffer
        int bufferSizeInSamples = AudioContext.BUFFER_SIZE * AudioContext.CHANNELS;
        this.floatBuffer = new float[bufferSizeInSamples];
        this.byteBuffer = new byte[bufferSizeInSamples * 2]; // 16-bit = 2 bytes per sample

        log.info("AudioIOManager creato");
    }

    //Inizializza ed avvia il sistema audio
    public synchronized void start() throws Exception{

        if (shouldRun){
            log.info("Audio già in esecuzione");
        }

        //Configura il formato audio
        AudioFormat format = createAudioFormat();

        //Trova ed apri la line audio
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)){
            throw new RuntimeException("Formato audio non supportato");
        }
        audioLine = (SourceDataLine) AudioSystem.getLine(info);
        audioLine.open(format, byteBuffer.length);

        //Avvia la linea vuota
        audioLine.start();

        //Avvia il thread audio
        shouldRun = true;
        audioThread = new Thread(this::audioLoop, "AudioThread");
        audioThread.setPriority(Thread.MAX_PRIORITY);
        audioThread.start();

        log.info("Sistema audio avviato");
        log.info("Formato: {}", format);
        log.info("Latenza stimata: {} ms", String.format("%.1f", AudioContext.BUFFER_DURATION_MS));

    }

    //ferma il sistema audio
    public synchronized void stop() {

        if (!shouldRun) {
            return;
        }

        log.info("Fermando il sistema audio");

        //ferma il loop
        shouldRun = false;

        //Aspetta che il thread finisca in base ai millisecondi segnati al join dal momento in cui viene chiamato
        try {
            if (audioThread != null){
                audioThread.join(1000);
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }

        //Chiudi la linea audio
        if (audioLine != null) {
            audioLine.stop();
            audioLine.close();
            audioLine = null;
        }

        log.info("Sistema audio fermato");
    }


    //Audio callBack loop
    //Cuore del sistema, loop infinito che riproduce audio in tempo reale!
    private void audioLoop() {
        log.info("Thread audio avviato");

        long frameCount = 0;

        while (shouldRun){
            try{
                //Chiedi all'audioEngine di riempire il buffer
                audioEngine.processAudio(floatBuffer);

                //Converti float -> byte per java sound
                convertFloatToBytes(floatBuffer,byteBuffer);

                //Invia i bytes alla scheda audio
                int bytesWritten = audioLine.write(byteBuffer,0, byteBuffer.length);

                //Statistiche di debug (ogni 1000 frame
                if (frameCount % 1000 == 0) {
                    debugInfo(frameCount,bytesWritten);
                }

                frameCount++;
            } catch (Exception e) {
                log.error("Errore nel loop audio: {}", e.getMessage());
                break;
            }
        }

        log.info("Thread audio terminato dopo: {} frame", frameCount);

    }


    //Metodi Utility

    //Crea il formato audio per Java sound
    private AudioFormat createAudioFormat() {
        return new AudioFormat(
                AudioContext.SAMPLE_RATE,           // Sample rate
                AudioContext.SAMPLE_SIZE_IN_BITS,   // Sample size in bits
                AudioContext.CHANNELS,              // Channels
                true,                               // Signed
                false                               // Little endian
        );
    }

    //Converte samples float in byte per Java
    private void convertFloatToBytes(float[] floatSamples, byte[] byteOutput) {
        for (int i = 0; i < floatSamples.length; i++) {
            // Converti float [-1.0, +1.0] → short [-32768, +32767]
            short shortSample = (short) (floatSamples[i] * 32767);

            // Little endian: byte basso prima, poi byte alto
            int byteIndex = i * 2;
            byteOutput[byteIndex] = (byte) (shortSample & 0xFF);           // Low byte
            byteOutput[byteIndex + 1] = (byte) ((shortSample >> 8) & 0xFF); // High byte
        }
    }

    private void debugInfo(long frameCount, int bytesWritten) {
        if (frameCount == 0) {
            System.out.println("Primo frame audio processato!");
        }

        // Ogni 10 secondi circa
        if (frameCount % 10000 == 0) {
            double secondsRunning = frameCount * AudioContext.BUFFER_DURATION_MS / 1000.0;
            System.out.println("Audio running for " +
                    String.format("%.1f", secondsRunning) + " seconds");
        }
    }

    public boolean isRunning() {
        return shouldRun;
    }

}
