package com.soundapp.music.core.engine;

import lombok.Setter;

/**
 * Kick drum sintetico: un SineOscillator la cui frequenza viene "tirata giù"
 * rapidamente da un pitch envelope, mentre un secondo envelope ne modula
 * l'ampiezza nel tempo. È lo stesso identico Envelope usato due volte per due
 * scopi diversi - qui la sua uscita 0.0-1.0 diventa un fattore di
 * interpolazione tra due frequenze, lì (ampiezza) è un moltiplicatore diretto
 * del segnale.
 *
 * È un oggetto "one-shot" e stateful: dopo trigger() gli envelope percorrono
 * le loro fasi fino a spegnersi (sustain = 0, quindi il colpo si esaurisce da
 * solo, senza bisogno di un noteOff()). Una volta arrivati a IDLE, ulteriori
 * chiamate a render() producono solo silenzio finché non richiami trigger().
 */
public class KickSynth implements AudioSource {

    @Setter
    private float startFrequency = 150.0f;
    @Setter
    private float endFrequency = 50.0f;

    private final SineOscillator oscillator = new SineOscillator();
    private final Envelope pitchEnvelope = new Envelope();
    private final Envelope ampEnvelope = new Envelope();

    public KickSynth() {
        // L'oscillatore resta al volume massimo consentito: è ampEnvelope,
        // non oscillator.volume, a disegnare la dinamica del colpo nel
        // tempo. Se lasciassimo il volume di default (0.3) staremmo
        // attenuando due volte lo stesso segnale, per un risultato più
        // debole e più difficile da regolare con un solo parametro.
        oscillator.setVolume(AudioContext.MAX_VOLUME);

        // Pitch envelope: nessun attacco (il pitch parte già al massimo),
        // decade fino a 0 (= endFrequency) e ci resta - un kick non "risale"
        // di tono dopo il colpo iniziale.
        pitchEnvelope.setAttack(0.0f);
        pitchEnvelope.setDecay(80.0f);
        pitchEnvelope.setSustain(0.0f);

        // Amp envelope: un attacco molto breve ma non zero (2ms) evita un
        // click digitale al primo sample; decade fino al silenzio (sustain
        // 0), che è la fine naturale di un suono percussivo.
        ampEnvelope.setAttack(2.0f);
        ampEnvelope.setDecay(250.0f);
        ampEnvelope.setSustain(0.0f);
    }

    public void setPitchDecay(float milliseconds) {
        pitchEnvelope.setDecay(milliseconds);
    }

    public void setAmpDecay(float milliseconds) {
        ampEnvelope.setDecay(milliseconds);
    }

    /**
     * Fa partire un nuovo colpo: riporta entrambi gli envelope in fase
     * Attack. Va richiamato prima di ogni render "indipendente" (es. prima
     * della preview e di nuovo prima dell'export su file), perché KickSynth
     * è stateful e un singolo trigger si esaurisce da solo.
     */
    public void trigger() {
        pitchEnvelope.noteOn();
        ampEnvelope.noteOn();
    }

    @Override
    public void render(float[] buffer) {
        // A differenza di SineOscillator.render() (che usa una frequenza
        // fissa per tutto il buffer), qui la frequenza cambia sample per
        // sample: dobbiamo quindi generare un frame alla volta, aggiornando
        // oscillator prima di ogni singolo frame invece che una volta sola
        // per l'intero buffer.
        float[] frame = new float[AudioContext.CHANNELS];

        for (int i = 0; i < buffer.length; i += AudioContext.CHANNELS) {
            float pitchAmount = pitchEnvelope.nextValue();
            float frequency = endFrequency + (startFrequency - endFrequency) * pitchAmount;
            oscillator.setFrequency(frequency);
            oscillator.render(frame);

            float amplitude = ampEnvelope.nextValue();
            for (int channel = 0; channel < AudioContext.CHANNELS; channel++) {
                buffer[i + channel] = frame[channel] * amplitude;
            }
        }
    }
}
