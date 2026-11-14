package com.soundapp.music.core.engine;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KickSynthTest {

    @Test
    void describesItsDefaultParameters_withTheSameVocabularyAsItsSetters() {
        Map<String, Object> parameters = new KickSynth().describeParameters();

        // Le chiavi sono volutamente i nomi dei setter pubblici: il file
        // generato deve essere leggibile mettendolo accanto al codice.
        assertEquals(150.0f, parameters.get("startFrequency"));
        assertEquals(50.0f, parameters.get("endFrequency"));
        assertEquals(80.0f, parameters.get("pitchDecay"));
        assertEquals(250.0f, parameters.get("ampDecay"));
    }

    @Test
    void describesBothInternalEnvelopes_nested() {
        Map<String, Object> parameters = new KickSynth().describeParameters();

        Map<?, ?> pitchEnvelope = assertInstanceOf(Map.class, parameters.get("pitchEnvelope"));
        Map<?, ?> ampEnvelope = assertInstanceOf(Map.class, parameters.get("ampEnvelope"));

        // Il pitch envelope del kick: nessun attacco (il pitch parte già in
        // alto) e sustain 0, cioè il colpo scende a endFrequency e ci resta.
        assertEquals(0.0f, pitchEnvelope.get("attack"));
        assertEquals(80.0f, pitchEnvelope.get("decay"));
        assertEquals(0.0f, pitchEnvelope.get("sustain"));

        // L'amp envelope: 2ms di attacco per non sentire il click iniziale,
        // poi decadimento fino al silenzio.
        assertEquals(2.0f, ampEnvelope.get("attack"));
        assertEquals(250.0f, ampEnvelope.get("decay"));
        assertEquals(0.0f, ampEnvelope.get("sustain"));

        // Il release c'è anche se il kick non lo usa (si esaurisce da solo,
        // senza noteOff): il dump descrive l'envelope completo, non solo le
        // manopole che giriamo oggi.
        assertTrue(pitchEnvelope.containsKey("release"));
        assertTrue(ampEnvelope.containsKey("release"));
    }

    @Test
    void describedParametersFollowTheSetters() {
        KickSynth kick = new KickSynth();
        kick.setStartFrequency(220.0f);
        kick.setEndFrequency(40.0f);
        kick.setPitchDecay(35.0f);
        kick.setAmpDecay(600.0f);

        Map<String, Object> parameters = kick.describeParameters();

        assertEquals(220.0f, parameters.get("startFrequency"));
        assertEquals(40.0f, parameters.get("endFrequency"));
        assertEquals(35.0f, parameters.get("pitchDecay"));
        assertEquals(600.0f, parameters.get("ampDecay"));

        // pitchDecay/ampDecay sono le manopole esterne, e dentro gli envelope
        // c'è lo stesso valore: le due viste non devono poter divergere.
        assertEquals(35.0f, ((Map<?, ?>) parameters.get("pitchEnvelope")).get("decay"));
        assertEquals(600.0f, ((Map<?, ?>) parameters.get("ampEnvelope")).get("decay"));
    }

    @Test
    void describesParametersWithoutTouchingTheSound() {
        KickSynth described = new KickSynth();
        KickSynth untouched = new KickSynth();

        described.trigger();
        untouched.trigger();

        float[] describedBuffer = new float[64 * AudioContext.CHANNELS];
        float[] untouchedBuffer = new float[64 * AudioContext.CHANNELS];

        // describeParameters() è una fotografia, non un comando: chiamarla
        // (anche a colpo già partito) non deve muovere lo stato degli
        // envelope, altrimenti il solo fatto di esportare il dump
        // cambierebbe il suono esportato.
        described.describeParameters();
        described.render(describedBuffer);
        untouched.render(untouchedBuffer);

        assertEquals(0, java.util.Arrays.compare(untouchedBuffer, describedBuffer),
                "il dump dei parametri non deve alterare i sample generati");
    }
}
