package com.soundapp.music.core.engine;

public class AudioContext {

    public static final int SAMPLE_RATE = 44100;

    public static final int CHANNELS = 2;

    public static final int SAMPLE_SIZE_IN_BITS = 16;

    public static final float MAX_VOLUME = 0.8f;

    public static final float MIN_FREQUENCY = 20.0f;

    public static final float MAX_FREQUENCY = 20000.0f;

    public static double frequencyToPhaseIncrement(double frequency) {
        return (2.0 * Math.PI * frequency) / SAMPLE_RATE;
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
