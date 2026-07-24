package com.soundapp.music.core.engine;

/**
 * Qualsiasi cosa sappia riempire un buffer interleaved di sample audio,
 * frame dopo frame - un oscillatore, un synth (oscillatore + envelope), o in
 * futuro qualcosa che ne combina più d'uno. È il contratto minimo che
 * AudioPreview e WavRenderer usano per non dover conoscere la classe
 * concreta di ciò che stanno ascoltando/esportando: non gli interessa se è
 * un semplice SineOscillator o un KickSynth più complesso, solo che sappia
 * produrre sample quando gli viene chiesto.
 */
public interface AudioSource {

    void render(float[] buffer);
}
