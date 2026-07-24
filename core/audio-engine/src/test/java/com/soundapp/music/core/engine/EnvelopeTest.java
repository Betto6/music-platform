package com.soundapp.music.core.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvelopeTest {

    @Test
    void clampsAtFullLevel_whenAttackCompletes() {
        Envelope envelope = new Envelope();
        envelope.setAttack(10.0f);   // 10ms = 441 sample a 44100 Hz
        envelope.setDecay(1000.0f);  // volutamente lungo, per non "invadere" questo test
        envelope.setSustain(0.0f);
        envelope.noteOn();

        float midpoint = 0f;
        float last = 0f;
        int samplesToCheck = 441 + 2; // +2 di margine per arrotondamenti float
        for (int i = 1; i <= samplesToCheck; i++) {
            last = envelope.nextValue();
            if (i == 220) { // circa a metà della rampa di attack
                midpoint = last;
            }
        }

        // La rampa è lineare: a metà strada temporale ci aspettiamo un
        // livello vicino a 0.5 (intervallo largo apposta, non è un test di
        // precisione ma di "sta davvero salendo gradualmente").
        assertTrue(midpoint > 0.4f && midpoint < 0.6f,
                "a metà dell'attack il livello dovrebbe essere vicino a 0.5, era " + midpoint);

        // Dopo che l'attacco è completato, il livello deve fermarsi
        // esattamente a 1.0 e non superarlo: è il clamp dentro nextValue()
        // a garantirlo quando currentLevel oltrepassa la soglia.
        assertEquals(1.0f, last, 0.0001f);
    }

    @Test
    void decaySettlesExactlyAtSustainLevel() {
        Envelope envelope = new Envelope();
        envelope.setAttack(0.0f);    // istantaneo: non ci interessa in questo test
        envelope.setDecay(10.0f);    // 10ms = 441 sample
        envelope.setSustain(0.3f);
        envelope.noteOn();

        float last = 0f;
        for (int i = 0; i < 441 + 2; i++) {
            last = envelope.nextValue();
        }

        assertEquals(0.3f, last, 0.0001f);

        // Il sustain deve TENERE il livello, non continuare a scendere: se
        // richiamiamo ancora nextValue() senza un noteOff(), deve restare fermo.
        assertEquals(0.3f, envelope.nextValue(), 0.0001f);
    }

    @Test
    void noteOffDuringAttack_startsReleaseFromCurrentLevel_notFromSustain() {
        Envelope envelope = new Envelope();
        envelope.setAttack(100.0f);  // lungo apposta: vogliamo restare "a metà" dell'attack
        envelope.setDecay(50.0f);
        envelope.setSustain(0.5f);   // più alto del livello che raggiungeremo prima del noteOff
        envelope.setRelease(50.0f);
        envelope.noteOn();

        float levelBeforeNoteOff = 0f;
        for (int i = 0; i < 10; i++) {
            levelBeforeNoteOff = envelope.nextValue();
        }
        // Con un attack di 100ms (4410 sample), dopo soli 10 sample siamo
        // ancora molto vicini a zero, ben al di sotto del sustain (0.5).
        assertTrue(levelBeforeNoteOff < 0.05f);

        envelope.noteOff();
        float afterNoteOff = envelope.nextValue();

        // Il punto della guardia: se il release ripartisse "dal sustain"
        // (bug plausibile: torno a 0.5 e poi rilascio da lì), il primo
        // valore dopo noteOff() salterebbe VERSO l'alto. Invece deve
        // continuare a scendere dolcemente da dov'era.
        assertTrue(afterNoteOff <= levelBeforeNoteOff,
                "il livello dopo noteOff() non deve risalire");
        assertTrue(afterNoteOff < 0.1f,
                "il release deve continuare da dove si trovava l'attack, non saltare verso il sustain (0.5)");
    }

    @Test
    void releaseReachesZero_andEnvelopeReturnsToSilence() {
        Envelope envelope = new Envelope();
        envelope.setAttack(0.0f);
        envelope.setDecay(0.0f);
        envelope.setSustain(0.4f);
        envelope.setRelease(10.0f); // 10ms = 441 sample
        envelope.noteOn();

        // attack=0 e decay=0 sono entrambi istantanei: bastano due chiamate
        // per arrivare in sustain (una fa scattare l'attack, la successiva
        // il decay - vedi il commento in Envelope.nextValue()).
        envelope.nextValue();
        envelope.nextValue();

        envelope.noteOff();
        float last = 0f;
        for (int i = 0; i < 441 + 2; i++) {
            last = envelope.nextValue();
        }

        assertEquals(0.0f, last, 0.0001f);
        // Deve restare a zero, non "rimbalzare" né andare in negativo se richiamato ancora.
        assertEquals(0.0f, envelope.nextValue(), 0.0001f);
    }

    @Test
    void sustainHoldsSteady_untilNoteOff() {
        Envelope envelope = new Envelope();
        envelope.setAttack(0.0f);
        envelope.setDecay(0.0f);
        envelope.setSustain(0.6f);
        envelope.noteOn();

        envelope.nextValue(); // attack istantaneo
        envelope.nextValue(); // decay istantaneo -> ora siamo in sustain

        // In sustain il livello non deve muoversi di una virgola, per
        // quante chiamate facciamo: nulla lo fa avanzare finché non arriva
        // un noteOff().
        for (int i = 0; i < 1000; i++) {
            assertEquals(0.6f, envelope.nextValue(), 0.0001f);
        }
    }

    @Test
    void sustainAtFullLevel_meansNoAudibleDecayDip() {
        Envelope envelope = new Envelope();
        envelope.setAttack(0.0f);
        envelope.setDecay(200.0f);
        envelope.setSustain(1.0f); // nessuna discesa: es. un organo, non un kick
        envelope.noteOn();

        float afterAttack = envelope.nextValue(); // attack istantaneo -> 1.0
        assertEquals(1.0f, afterAttack, 0.0001f);

        // Con sustain=1.0 il decay non ha nulla da percorrere (range = 1.0
        // - sustain = 0): il livello deve restare a 1.0 fin da subito,
        // senza scendere per poi tornare su.
        float nextSample = envelope.nextValue();
        assertEquals(1.0f, nextSample, 0.0001f);
    }
}
