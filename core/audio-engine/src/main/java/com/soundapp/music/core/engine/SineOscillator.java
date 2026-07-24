package com.soundapp.music.core.engine;

import lombok.Getter;

/**
 * Synth object di base: oscillatore sinusoidale.
 * Genera samples in un buffer dato, senza alcuna nozione di
 * playback real-time: chi lo chiama (renderer/composition) decide quando
 * e per quanti sample farlo generare.
 */
public class SineOscillator {

    @Getter
    private float frequency = 440.0f;

    @Getter
    private float volume = 0.3f;

    private double phase = 0.0;
    private double phaseIncrement;

    public SineOscillator() {
        updatePhaseIncrement();
    }

    public void setFrequency(float frequency) {
        this.frequency = AudioContext.clamp(frequency, AudioContext.MIN_FREQUENCY, AudioContext.MAX_FREQUENCY);
        updatePhaseIncrement();
    }

    public void setVolume(float volume) {
        this.volume = AudioContext.clamp(volume, 0.0f, AudioContext.MAX_VOLUME);
    }

    /**
     * Riempie il buffer (interleaved, {@link AudioContext#CHANNELS} canali) con i sample generati.
     */
    public void render(float[] buffer) {
        for (int i = 0; i < buffer.length; i += AudioContext.CHANNELS) {
            float sample = (float) (Math.sin(phase) * volume);

            for (int channel = 0; channel < AudioContext.CHANNELS; channel++) {
                buffer[i + channel] = sample;
            }

            phase += phaseIncrement;
            if (phase >= 2 * Math.PI) {
                phase -= 2 * Math.PI;
            }
        }
    }

    private void updatePhaseIncrement() {
        this.phaseIncrement = AudioContext.frequencyToPhaseIncrement(frequency);
    }
}
