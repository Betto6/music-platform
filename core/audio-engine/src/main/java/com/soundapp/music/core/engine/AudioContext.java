package com.soundapp.music.core.engine;

public class AudioContext {

    private AudioContext() {
    }

    public static final int SAMPLE_RATE = 44100;

    public static final int CHANNELS = 2;

    public static final int SAMPLE_SIZE_IN_BITS = 16;

    public static final float MAX_VOLUME = 0.8f;

    public static final float MIN_FREQUENCY = 20.0f;

    public static final float MAX_FREQUENCY = 20000.0f;

    public static double frequencyToPhaseIncrement(double frequency) {
        return (2.0 * Math.PI * frequency) / SAMPLE_RATE;
    }

    //Il basso techno vive tra La1 (55 Hz) e La2 (110 Hz).
    // Il kick fondamentale è spesso intorno a 50–60 Hz con il pitch che scende da ~150 Hz in ~80ms —
    // per questo midiToFrequency diventa essenziale appena implementi KickSynth e AcidBass
    public static float midiToFrequency(int midiNote) {
        return (float)(440.0 * Math.pow(2.0, (midiNote - 69) / 12.0));
    }

    public static float clamp(float value, float min, float max) {
        return Math.clamp(value, min, max);
    }
}
