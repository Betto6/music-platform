package com.soundapp.music.audio;

import org.springframework.stereotype.Component;

@Component
public class AudioContext {

    private int sampleRate;
    private int bufferSize;
    private int channels;

    public AudioContext() {
        // Valori di default
        this.sampleRate = 44100;  // CD quality
        this.bufferSize = 512;    // Low latency
        this.channels = 2;        // Stereo
        System.out.println("AudioContext inizializzato con configurazione default");
    }

    // Getters
    public int getSampleRate() {
        return sampleRate;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getChannels() {
        return channels;
    }

    // Setters
    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    // Utility method
    public double getLatencyMs() {
        return (double) bufferSize / sampleRate * 1000.0;
    }

    @Override
    public String toString() {
        return String.format("AudioContext [%dHz, buffer=%d, channels=%d, latency=%.2fms]",
                sampleRate, bufferSize, channels, getLatencyMs());
    }
}