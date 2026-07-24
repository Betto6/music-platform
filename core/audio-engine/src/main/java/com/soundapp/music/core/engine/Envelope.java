package com.soundapp.music.core.engine;

/**
 * Inviluppo ADSR (Attack-Decay-Sustain-Release): descrive come l'ampiezza di
 * un suono cambia nel tempo, dal momento in cui una nota parte (noteOn) a
 * quando si spegne del tutto dopo il rilascio (noteOff + tempo di release).
 *
 * A differenza di SineOscillator, che genera un'onda continua senza un vero
 * "inizio" o "fine", l'envelope è intrinsecamente uno stato che cambia fase
 * nel tempo: per questo internamente tiene traccia sia della fase attuale
 * (ATTACK, DECAY, ...) sia del livello di ampiezza raggiunto in quel momento.
 *
 * Nota: le rampe qui sono LINEARI (l'ampiezza cresce/scende a step costante).
 * Un envelope "professionale" userebbe curve esponenziali, che l'orecchio
 * percepisce come più naturali - ma è un raffinamento, non il ciclo base:
 * lo aggiungeremo solo se, ascoltando, la versione lineare suonerà innaturale.
 */
public class Envelope {

    // Le fasi ADSR, più IDLE: lo stato "a riposo" prima del primo noteOn()
    // e dopo che il release è arrivato a zero.
    private enum Stage {
        IDLE, ATTACK, DECAY, SUSTAIN, RELEASE
    }

    private float attackMillis = 10.0f;
    private float decayMillis = 100.0f;
    private float sustainLevel = 0.7f;
    private float releaseMillis = 200.0f;

    private Stage stage = Stage.IDLE;

    // Livello di ampiezza corrente (0.0-1.0): è il valore che nextValue()
    // restituisce e aggiorna ad ogni chiamata. Lo teniamo come stato (e non
    // lo ricalcoliamo da zero ogni volta) perché noteOff() può arrivare in
    // un punto qualsiasi di attack/decay/sustain, non solo a sustain
    // raggiunto: il release deve sempre ripartire dal livello ATTUALE.
    private float currentLevel = 0.0f;

    // Quanto cresce/scende currentLevel ad ogni singolo sample, nella fase
    // attualmente attiva. Viene ricalcolato ogni volta che si entra in una
    // nuova fase, in base a quanti sample dura quella fase a questo sample
    // rate (vedi millisToStep).
    private float currentStep = 0.0f;

    public void setAttack(float milliseconds) {
        this.attackMillis = Math.max(0.0f, milliseconds);
    }

    public void setDecay(float milliseconds) {
        this.decayMillis = Math.max(0.0f, milliseconds);
    }

    public void setSustain(float level) {
        this.sustainLevel = AudioContext.clamp(level, 0.0f, 1.0f);
    }

    public void setRelease(float milliseconds) {
        this.releaseMillis = Math.max(0.0f, milliseconds);
    }

    /**
     * Fa partire l'envelope da zero: entra in fase Attack.
     * Semplificazione nota: se noteOn() arriva mentre l'envelope è già a un
     * livello alto (retrigger prima che una nota precedente sia finita), qui
     * si riparte comunque da 0, il che può produrre un piccolo click. Non lo
     * risolviamo ora: il primo utilizzo (KickSynth) triggera una nota alla
     * volta con silenzio tra un colpo e l'altro, quindi il caso non si
     * presenta ancora.
     */
    public void noteOn() {
        stage = Stage.ATTACK;
        currentLevel = 0.0f;
        currentStep = millisToStep(attackMillis, 1.0f - currentLevel);
    }

    /**
     * Interrompe attack/decay/sustain e forza l'inizio della fase Release,
     * partendo dal livello raggiunto in quel momento (non da sustainLevel).
     */
    public void noteOff() {
        stage = Stage.RELEASE;
        currentStep = millisToStep(releaseMillis, currentLevel);
    }

    /**
     * Restituisce il moltiplicatore d'ampiezza (0.0-1.0) per il campione
     * corrente, e avanza lo stato interno di un frame. Va chiamata una volta
     * per frame, nello stesso ordine in cui l'oscillatore genera i suoi
     * sample, e il risultato va moltiplicato per l'uscita dell'oscillatore.
     */
    public float nextValue() {
        switch (stage) {
            case IDLE -> currentLevel = 0.0f;

            case ATTACK -> {
                currentLevel += currentStep;
                if (currentLevel >= 1.0f) {
                    currentLevel = 1.0f;
                    stage = Stage.DECAY;
                    currentStep = millisToStep(decayMillis, 1.0f - sustainLevel);
                }
            }

            case DECAY -> {
                currentLevel -= currentStep;
                if (currentLevel <= sustainLevel) {
                    currentLevel = sustainLevel;
                    stage = Stage.SUSTAIN;
                }
            }

            // In sustain il livello resta fermo: non c'è nulla da far avanzare
            // finché non arriva un noteOff() a spostarci in RELEASE.
            case SUSTAIN -> currentLevel = sustainLevel;

            case RELEASE -> {
                currentLevel -= currentStep;
                if (currentLevel <= 0.0f) {
                    currentLevel = 0.0f;
                    stage = Stage.IDLE;
                }
            }
        }
        return currentLevel;
    }

    /**
     * Converte una durata in millisecondi nell'incremento (o decremento) da
     * applicare a currentLevel ad ogni singolo sample, per coprire "range" di
     * ampiezza in quel tempo, a AudioContext.SAMPLE_RATE campioni al secondo.
     * Esempio: attack di 10ms con range 1.0 a 44100 Hz -> circa 441 sample
     * per arrivare da 0 a 1, quindi step = 1.0 / 441 ≈ 0.0023 per sample.
     */
    private float millisToStep(float millis, float range) {
        if (millis <= 0.0f) {
            // Fase istantanea: copriamo tutto il range in un solo sample,
            // altrimenti divideremmo per zero.
            return range;
        }
        float totalSamples = (millis / 1000.0f) * AudioContext.SAMPLE_RATE;
        return range / totalSamples;
    }
}
