package com.soundapp.music.core;



public class AudioContext {

    // === PARAMETRI AUDIO BASE ===

    /** Frequenza di campionamento (Hz) - Standard CD quality */
    public static final int SAMPLE_RATE = 44100;

    /** Dimensione buffer in samples - Bilancia latenza/stabilità */
    public static final int BUFFER_SIZE = 512;

    /** Numero di canali audio (2 = stereo) */
    public static final int CHANNELS = 2;

    /** Bit depth per sample (16-bit standard) */
    public static final int SAMPLE_SIZE_IN_BITS = 16;

    // === CALCOLI DERIVATI ===

    /** Durata di un buffer in millisecondi */
    public static final double BUFFER_DURATION_MS =
            (double) BUFFER_SIZE / SAMPLE_RATE * 1000.0;

    /** Frequenza di aggiornamento buffer (Hz) */
    public static final double BUFFER_RATE_HZ =
            (double) SAMPLE_RATE / BUFFER_SIZE;

    // === LIMITI DI SICUREZZA ===

    /** Volume massimo per evitare danni all'udito */
    public static final float MAX_VOLUME = 0.8f;

    /** Frequenza minima generabile */
    public static final float MIN_FREQUENCY = 20.0f;

    /** Frequenza massima generabile */
    public static final float MAX_FREQUENCY = 20000.0f;

    /**
     * Converte una frequenza in incremento di fase per oscillatori
     * @param frequency Frequenza in Hz
     * @return Incremento di fase per sample
     */
    public static double frequencyToPhaseIncrement(double frequency) {
        return (2.0 * Math.PI * frequency) / SAMPLE_RATE;
    }

    /**
     * Clamps un valore tra min e max
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Stampa info sistema audio per debug
     */
    public static void printAudioInfo() {
        System.out.println("=== AUDIO CONTEXT INFO ===");
        System.out.println("Sample Rate: " + SAMPLE_RATE + " Hz");
        System.out.println("Buffer Size: " + BUFFER_SIZE + " samples");
        System.out.println("Channels: " + CHANNELS);
        System.out.println("Buffer Duration: " + String.format("%.2f", BUFFER_DURATION_MS) + " ms");
        System.out.println("Buffer Rate: " + String.format("%.2f", BUFFER_RATE_HZ) + " Hz");
        System.out.println("Max Volume: " + MAX_VOLUME);
        System.out.println("========================");
    }
}