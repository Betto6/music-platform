package com.soundapp.music.core.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AudioContextTest {

    @Test
    void clamp_returnsValueUnchanged_whenWithinRange() {
        assertEquals(5.0f, AudioContext.clamp(5.0f, 0.0f, 10.0f), 0.0001f);
    }

    @Test
    void clamp_returnsMin_whenBelowRange() {
        assertEquals(0.0f, AudioContext.clamp(-3.0f, 0.0f, 10.0f), 0.0001f);
    }

    @Test
    void clamp_returnsMax_whenAboveRange() {
        assertEquals(10.0f, AudioContext.clamp(99.0f, 0.0f, 10.0f), 0.0001f);
    }

    @Test
    void frequencyToPhaseIncrement_matchesFormula() {
        double frequency = 440.0;
        double expected = (2.0 * Math.PI * frequency) / AudioContext.SAMPLE_RATE;

        assertEquals(expected, AudioContext.frequencyToPhaseIncrement(frequency), 0.000001);
    }

    @Test
    void midiToFrequency_returnsConcertPitch_forMidiNote69() {
        // Il La4 (440 Hz, il diapason standard a cui si accordano gli
        // strumenti) è per convenzione la nota MIDI 69: è il punto di
        // riferimento da cui la formula ricava tutte le altre frequenze.
        assertEquals(440.0f, AudioContext.midiToFrequency(69), 0.01f);
    }

    @Test
    void midiToFrequency_doublesFrequency_oneOctaveHigher() {
        // Un'ottava sopra significa il doppio della frequenza: è una legge
        // fisica dell'udito, non un dettaglio implementativo. Verificarla
        // conferma che la formula (basata su potenze di 2) sia corretta,
        // senza dover fissare a mano ogni singolo valore atteso.
        float note69 = AudioContext.midiToFrequency(69);
        float note81 = AudioContext.midiToFrequency(81); // +12 semitoni = un'ottava

        assertEquals(note69 * 2.0f, note81, 0.01f);
    }
}
