package com.soundapp.music.core.render;

import com.soundapp.music.core.engine.AudioContext;
import com.soundapp.music.core.engine.AudioSource;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Materializza un oscillatore come audio in tempo reale sulla scheda audio,
 * invece che su file WAV. Stesso concetto di un renderer offline, ma con lo
 * speaker come sink al posto del disco.
 */
public class AudioPreview {

    private static final int FRAMES_PER_CHUNK = 512;

    public void preview(AudioSource source, float durationSeconds) {
        AudioFormat format = new AudioFormat(
                AudioContext.SAMPLE_RATE,
                AudioContext.SAMPLE_SIZE_IN_BITS,
                AudioContext.CHANNELS,
                true,
                false);

        float[] sampleBuffer = new float[FRAMES_PER_CHUNK * AudioContext.CHANNELS];
        byte[] byteBuffer = new byte[sampleBuffer.length * 2];

        int totalFrames = (int) (AudioContext.SAMPLE_RATE * durationSeconds);
        int framesRendered = 0;

        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            line.open(format);
            line.start();

            while (framesRendered < totalFrames) {
                source.render(sampleBuffer);
                toPcm16Bytes(sampleBuffer, byteBuffer);
                line.write(byteBuffer, 0, byteBuffer.length);
                framesRendered += FRAMES_PER_CHUNK;
            }

            line.drain();
        } catch (LineUnavailableException e) {
            throw new IllegalStateException("Nessuna scheda audio disponibile per la preview", e);
        }
    }

    private void toPcm16Bytes(float[] samples, byte[] out) {
        for (int i = 0; i < samples.length; i++) {
            short pcm = (short) (AudioContext.clamp(samples[i], -1.0f, 1.0f) * Short.MAX_VALUE);
            out[i * 2] = (byte) (pcm & 0xFF);
            out[i * 2 + 1] = (byte) ((pcm >> 8) & 0xFF);
        }
    }
}
