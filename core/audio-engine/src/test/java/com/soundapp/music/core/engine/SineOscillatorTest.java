package com.soundapp.music.core.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SineOscillatorTest {

    @Test
    void setFrequency_clampsBelowMinimum() {
        SineOscillator oscillator = new SineOscillator();
        oscillator.setFrequency(5.0f); // sotto MIN_FREQUENCY (20 Hz)

        assertEquals(AudioContext.MIN_FREQUENCY, oscillator.getFrequency(), 0.0001f);
    }

    @Test
    void setFrequency_clampsAboveMaximum() {
        SineOscillator oscillator = new SineOscillator();
        oscillator.setFrequency(30000.0f); // sopra MAX_FREQUENCY (20000 Hz)

        assertEquals(AudioContext.MAX_FREQUENCY, oscillator.getFrequency(), 0.0001f);
    }

    @Test
    void setVolume_clampsAboveMaximum() {
        SineOscillator oscillator = new SineOscillator();
        oscillator.setVolume(5.0f); // sopra MAX_VOLUME (0.8)

        assertEquals(AudioContext.MAX_VOLUME, oscillator.getVolume(), 0.0001f);
    }

    @Test
    void setVolume_clampsBelowZero() {
        SineOscillator oscillator = new SineOscillator();
        oscillator.setVolume(-1.0f);

        assertEquals(0.0f, oscillator.getVolume(), 0.0001f);
    }

    @Test
    void render_neverExceedsConfiguredVolume() {
        SineOscillator oscillator = new SineOscillator();
        oscillator.setFrequency(440.0f);
        oscillator.setVolume(0.5f);

        float[] buffer = new float[1000 * AudioContext.CHANNELS];
        oscillator.render(buffer);

        // Un seno di ampiezza "volume" non può mai superare, in valore
        // assoluto, il volume stesso: se un sample lo facesse, vorrebbe dire
        // che il moltiplicatore per il volume non viene applicato bene.
        for (float sample : buffer) {
            assertTrue(Math.abs(sample) <= 0.5f + 0.0001f, "sample fuori range: " + sample);
        }
    }

    @Test
    void render_writesSameValueToBothChannels() {
        SineOscillator oscillator = new SineOscillator();
        oscillator.setFrequency(440.0f);

        float[] buffer = new float[100 * AudioContext.CHANNELS];
        oscillator.render(buffer);

        // Il rendering è "interleaved": ogni frame contiene CHANNELS sample
        // consecutivi. Per un oscillatore mono duplicato sui due canali, i
        // due valori dello stesso frame devono essere identici bit per bit.
        for (int i = 0; i < buffer.length; i += AudioContext.CHANNELS) {
            assertEquals(buffer[i], buffer[i + 1], 0.0f);
        }
    }

    @Test
    void render_continuesPhase_acrossMultipleCalls() {
        SineOscillator continuous = new SineOscillator();
        continuous.setFrequency(440.0f);
        float[] wholeBuffer = new float[8 * AudioContext.CHANNELS];
        continuous.render(wholeBuffer);

        SineOscillator splitOscillator = new SineOscillator();
        splitOscillator.setFrequency(440.0f);
        float[] firstHalf = new float[4 * AudioContext.CHANNELS];
        float[] secondHalf = new float[4 * AudioContext.CHANNELS];
        splitOscillator.render(firstHalf);
        splitOscillator.render(secondHalf);

        // Questa è esattamente la proprietà che KickSynth sfrutta per
        // modulare la frequenza sample per sample: chiamare render() più
        // volte con buffer piccoli deve dare lo STESSO risultato che
        // chiamarlo una volta sola con un buffer grande, perché la fase
        // persiste tra le chiamate invece di ripartire da zero ogni volta.
        for (int i = 0; i < secondHalf.length; i++) {
            assertEquals(wholeBuffer[firstHalf.length + i], secondHalf[i], 0.0f);
        }
    }
}
