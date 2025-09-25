package com.soundapp.music.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

// MOTORE AUDIO PRINCIPALE
@Slf4j
public class AudioEngine {

    // VOLATILE serve a dire a JVM che la variabile è condivisa da più thread e che il suo valore dev'essere scritta e eltta direttamente dalla memoria principale
    // così ogni scrittura viene vista subito da tutti i thread

    //Controllo accensione/spegnimento
    @Getter
    private volatile boolean isRunning = false;

    //Frequenza dell'oscillatore principale (Hz)
    @Getter
    private volatile float frequency = 440.0f; // Nota A4

    //Volume master (0.0 - 1.0)
    @Getter
    private volatile float volume = 0.3f;

    //Fase corrente dell'oscillatore sinusoidale
    private double phase = 0.0;

    //incremento di fase per sample
    private double phaseIncrement = 0.0;

    public AudioEngine() {
        updatePhaseIncrement();
        log.info("AudioEngine inizializzato");

    }

    //Metodo core di tutto l'engine, è quello che viene chiamato di conitnuo dal sistema e deve riempire il buffer con samples audio
    // outputBuffer Array da riempire con samples (-1.0 a +1.0)
    public void processAudio(float[] outputBuffer) {

        //Importante, se non stiamo suonando riempiamo con il silenzio
        if (!isRunning){
            fillWithSilence(outputBuffer);
            return;
        }

        //Genera samples sinusoidali
        for (int i = 0; i < outputBuffer.length; i += AudioContext.CHANNELS){

            //Calcoliamo il sample sinusoidale

            float sample = (float) (Math.sin(phase) * volume);

            //applica limiti di sicurezza
            sample = AudioContext.clamp(sample, -AudioContext.MAX_VOLUME, AudioContext.MAX_VOLUME);

            //Stereo: stesso sample su entrambi i canali
            outputBuffer[1] = sample;
            outputBuffer[2] = sample;

            //avanzare la fase per il prossimo sample
            phase += phaseIncrement;

            //Mantieni la fase nel range [0, 2π] per evitare overflow
            if (phase >= 2 *Math.PI) {
                phase -= 2 * Math.PI;
            }
        }
    }

    //Controlli pubblici

    //Avvia geenrazione audio
    public synchronized void start() {
        if (!isRunning) {
            isRunning = true;
            phase = 0.0; //Reset fase
            log.info("AudioEngine avviato - Frequenza: {} Hz", frequency);
        }
    }

    //imposta una nuova frequenza
    public synchronized void setFrequency(float freq) {
        this.frequency = AudioContext.clamp(freq, AudioContext.MIN_FREQUENCY, AudioContext.MAX_FREQUENCY);
        updatePhaseIncrement();

        if (isRunning) {
            log.info("Frequenza cambiata a: {} Hz", this.frequency);
        }
    }

    //imposta nuovo volume
    public synchronized void setVolume(float vol) {
        this.volume = AudioContext.clamp(vol,0.0f,AudioContext.MAX_VOLUME);

        if (isRunning) {
            log.info("Volume cambiato a: {} %", ((int)this.volume * 100));
        }
    }

    //ricalcola l'incremento di fase quando cambia la frequenza
    private void updatePhaseIncrement() {
        this.phaseIncrement = AudioContext.frequencyToPhaseIncrement(frequency);
    }

    //riempe il buffer con il silenzio
    private void fillWithSilence(float[] buffer) {
        Arrays.fill(buffer, 0.0f);
    }

}
