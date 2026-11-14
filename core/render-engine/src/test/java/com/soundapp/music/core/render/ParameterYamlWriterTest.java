package com.soundapp.music.core.render;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParameterYamlWriterTest {

    private final ParameterYamlWriter writer = new ParameterYamlWriter();

    @Test
    void writesFlatValues_oneKeyPerLine_inInsertionOrder() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("source", "KickSynth");
        values.put("sampleRate", 44100);
        values.put("channels", 2);

        String yaml = writer.toYaml(values);

        // L'ordine non è un dettaglio estetico: la mappa è una LinkedHashMap
        // proprio perché il file va riletto da un umano, e le chiavi devono
        // comparire nell'ordine in cui il componente le ha dichiarate.
        assertEquals(lines("source: KickSynth", "sampleRate: 44100", "channels: 2"), yaml);
    }

    @Test
    void writesNestedMap_indentedUnderItsKey() {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("attack", 2.0f);
        envelope.put("decay", 250.0f);

        Map<String, Object> values = new LinkedHashMap<>();
        values.put("startFrequency", 150.0f);
        values.put("ampEnvelope", envelope);

        String yaml = writer.toYaml(values);

        assertEquals(lines(
                "startFrequency: 150.0",
                "ampEnvelope:",
                "  attack: 2.0",
                "  decay: 250.0"), yaml);
    }

    @Test
    void formatsFloats_withoutBinaryNoise_andAlwaysWithOneDecimal() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("volume", 0.3f);          // il classico float che in double diventa 0.30000001192092896
        values.put("sustain", 0.0f);
        values.put("startFrequency", 150.0f);
        values.put("durationSeconds", 1.5f);

        String yaml = writer.toYaml(values);

        assertEquals(lines(
                "volume: 0.3",
                "sustain: 0.0",
                "startFrequency: 150.0",
                "durationSeconds: 1.5"), yaml);
    }

    @Test
    void formatsSmallFloats_inPlainNotation_notScientific() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("tinyStep", 0.0000023f);

        // Float.toString darebbe "2.3E-6": legale in YAML, ma illeggibile
        // accanto a valori come "150.0".
        assertEquals(lines("tinyStep: 0.0000023"), writer.toYaml(values));
    }

    @Test
    void keepsIntegersWithoutDecimalPoint() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("sampleRate", 44100);
        values.put("frames", 66150L);

        assertEquals(lines("sampleRate: 44100", "frames: 66150"), writer.toYaml(values));
    }

    @Test
    void leavesPlainStringsUnquoted_butProtectsAmbiguousOnes() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("source", "KickSynth");
        values.put("renderedAt", "2026-11-14T10:23:45+01:00");
        values.put("note", "");
        values.put("comment", "attenzione: qui serve il quoting");
        values.put("dashed", "- non una lista");

        String yaml = writer.toYaml(values);

        assertEquals(lines(
                "source: KickSynth",
                // il timestamp ISO ha dei ':' ma non ': ', quindi non è ambiguo
                "renderedAt: 2026-11-14T10:23:45+01:00",
                "note: \"\"",
                "comment: \"attenzione: qui serve il quoting\"",
                "dashed: \"- non una lista\""), yaml);
    }

    @Test
    void writesNullAsYamlNull() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("unset", null);

        assertEquals(lines("unset: null"), writer.toYaml(values));
    }

    @Test
    void rejectsNestingDeeperThanOneLevel() {
        Map<String, Object> tooDeep = new LinkedHashMap<>();
        tooDeep.put("inner", new LinkedHashMap<>(Map.of("key", "value")));

        Map<String, Object> values = new LinkedHashMap<>();
        values.put("outer", tooDeep);

        // Meglio fallire subito che scrivere un file silenziosamente storto:
        // chi chiama (WavRenderer) intercetta comunque l'errore e non fa
        // fallire l'export del WAV.
        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> writer.toYaml(values));
        assertTrue(error.getMessage().contains("outer.inner"), error.getMessage());
    }

    private String lines(String... yamlLines) {
        return String.join(System.lineSeparator(), yamlLines) + System.lineSeparator();
    }
}
