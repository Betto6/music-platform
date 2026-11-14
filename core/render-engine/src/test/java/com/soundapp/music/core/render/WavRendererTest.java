package com.soundapp.music.core.render;

import com.soundapp.music.core.engine.AudioSource;
import com.soundapp.music.core.engine.KickSynth;
import com.soundapp.music.core.engine.SineOscillator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WavRendererTest {

    private static final float SHORT_DURATION = 0.05f;

    @Test
    void writesTheParameterDumpNextToTheWav(@TempDir Path outputDir) throws IOException {
        KickSynth kick = new KickSynth();
        kick.trigger();

        Path wav = outputDir.resolve("kick_dark.wav");
        new WavRenderer().render(kick, SHORT_DURATION, wav);

        Path dump = outputDir.resolve("kick_dark.yml");
        assertTrue(Files.exists(wav), "il WAV deve essere stato scritto");
        assertTrue(Files.exists(dump), "accanto al WAV deve comparire il dump con lo stesso basename");

        String yaml = Files.readString(dump, StandardCharsets.UTF_8);

        // I parametri della source, col vocabolario dei suoi setter.
        assertTrue(yaml.contains("startFrequency: 150.0"), yaml);
        assertTrue(yaml.contains("endFrequency: 50.0"), yaml);
        assertTrue(yaml.contains("pitchDecay: 80.0"), yaml);
        assertTrue(yaml.contains("ampDecay: 250.0"), yaml);

        // Gli envelope interni, annidati sotto la loro chiave.
        assertTrue(yaml.contains("pitchEnvelope:" + System.lineSeparator() + "  attack: 0.0"), yaml);
        assertTrue(yaml.contains("ampEnvelope:" + System.lineSeparator() + "  attack: 2.0"), yaml);

        // I metadati del render: senza di questi il file direbbe come era
        // configurato il synth, ma non in che formato è uscito il suono.
        assertTrue(yaml.contains("source: KickSynth"), yaml);
        assertTrue(yaml.contains("durationSeconds: 0.05"), yaml);
        assertTrue(yaml.contains("sampleRate: 44100"), yaml);
        assertTrue(yaml.contains("channels: 2"), yaml);
        assertTrue(yaml.contains("renderedAt: "), yaml);
    }

    @Test
    void dumpsTheParametersActuallyUsed_notTheDefaults(@TempDir Path outputDir) throws IOException {
        SineOscillator oscillator = new SineOscillator();
        oscillator.setFrequency(110.0f);
        oscillator.setVolume(0.5f);

        Path wav = outputDir.resolve("bass.wav");
        new WavRenderer().render(oscillator, SHORT_DURATION, wav);

        String yaml = Files.readString(outputDir.resolve("bass.yml"), StandardCharsets.UTF_8);

        assertTrue(yaml.contains("frequency: 110.0"), yaml);
        assertTrue(yaml.contains("volume: 0.5"), yaml);
    }

    @Test
    void writesOnlyTheWav_whenTheSourceCannotDescribeItself(@TempDir Path outputDir) {
        // Una source "muta": implementa solo AudioSource. Non deve essere un
        // errore - semplicemente non c'è nulla da documentare.
        AudioSource silence = buffer -> {
        };

        Path wav = outputDir.resolve("silence.wav");
        new WavRenderer().render(silence, SHORT_DURATION, wav);

        assertTrue(Files.exists(wav), "il WAV deve essere stato scritto comunque");
        assertFalse(Files.exists(outputDir.resolve("silence.yml")),
                "senza parametri da dichiarare non va scritto alcun dump");
    }

    @Test
    void exportsTheWavEvenWhenTheDumpCannotBeWritten(@TempDir Path outputDir) throws IOException {
        KickSynth kick = new KickSynth();
        kick.trigger();

        // Occupiamo il percorso del dump con una DIRECTORY: Files.writeString
        // non potrà scriverci. Il WAV è il deliverable e deve uscire comunque.
        Files.createDirectory(outputDir.resolve("blocked.yml"));

        Path wav = outputDir.resolve("blocked.wav");
        new WavRenderer().render(kick, SHORT_DURATION, wav);

        assertTrue(Files.exists(wav), "un dump non scrivibile non deve far fallire l'export del WAV");
        assertTrue(Files.size(wav) > 0, "il WAV non deve essere vuoto");
    }
}
