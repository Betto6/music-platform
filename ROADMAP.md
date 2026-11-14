# Roadmap — SoundApp

> Piano di studio e implementazione, ordinato per **catena del segnale** e
> **risultato udibile**, non secondo l'ordine di lettura del Cosimi.
> I riferimenti di pagina rimandano a [`COSIMI_INDEX.md`](COSIMI_INDEX.md);
> il metodo di lavoro per ogni singolo componente è in
> [`IMPLEMENTATION_GUIDE.md`](IMPLEMENTATION_GUIDE.md).

---

## Cos'è SoundApp (e cosa non è)

SoundApp è una **sound design station offline**: progetta, sintetizza ed
esporta file WAV — one-shot, campionature, piccoli loop.

Il live **non si fa dentro SoundApp**. I WAV generati vengono caricati in uno
strumento dedicato (SuperCollider, TidalCycles, una DAW, un sampler hardware)
e suonati da lì.

```
SoundApp (Java)                    Strumento live
─────────────────                  ──────────────────────
progetta il suono                  carica i WAV
lo renderizza in WAV        ──►    li triggera a tempo
esporta in una cartella            applica effetti, performa
```

L'integrazione tra i due mondi è **una cartella condivisa**, niente di più.
Hot reload su filesystem e comunicazione OSC sono rimandati e fuori da questa
roadmap: reintrodurrebbero i vincoli real-time che lo scope offline elimina.

---

## I due binari

| Progetto | Ruolo |
|---|---|
| `dsp-exercises` | **Playground.** Un concetto Cosimi alla volta, isolato, con unit test. Qui si può sbagliare. |
| `soundapp` | **Implementazione.** Si tocca solo dopo che l'esercizio corrispondente è verde. |

**Regola dura, non linea guida:** nessun concetto entra in SoundApp prima di
essere stato metabolizzato con esercizi isolati e test passanti.

Ciclo per ogni fase:

```
leggere Cosimi  →  esercizi in dsp-exercises (verdi)  →  implementare in SoundApp
```

---

## Criterio di ordinamento

Ogni fase deve chiudersi con **un WAV che suona meglio o più completo** di
quello della fase precedente.

Da qui la deviazione principale rispetto all'ordine del libro: gli **Envelope**
(Cosimi p. 80) vengono **prima dei Filtri** (p. 49). Un filtro senza un envelope
che ne moduli il cutoff è statico, e un suono statico non si verifica
all'orecchio — che è il criterio della Fase E dell'implementation guide.

Nessuna fase dipende da una successiva. L'ordine è pensato per non dover
tornare indietro a riscrivere.

---

## Le fasi

```
1 Oscillatori → 2 Envelope → 3 Amplifier          la voce minima
4 Noise       → 5 Drum synthesis                  i primi suoni veri
6 Filtri      → 7 Waveshaping                     il carattere
8 Mixer       → 9 LFO                             più voci, movimento
10 Sequencer  → 11 Controllers → 12 Voci          la struttura
```

---

### Fase 0 — Inquadramento *(solo lettura)*

| | |
|---|---|
| **Cosimi** | Primo Inquadramento (pp. 6–18) + Concetti Base (pp. 19–26) |
| **Playground** | nessun esercizio |
| **SoundApp** | niente |
| **Risultato** | vocabolario comune: CV/Gate/Trigger, categorie funzionali, patch chart |

---

### Fase 1 — Oscillatori

| | |
|---|---|
| **Cosimi** | Oscillator (pp. 28–38) + Oscillatori speciali (pp. 39–41) |
| **Playground** | sine / triangle / saw / square band-limited, PWM, hard & soft sync, FM lineare ed esponenziale |
| **SoundApp** | estensione di `SineOscillator`, nuovi oscillatori anti-alias |
| **Risultato** | un WAV con le quattro forme d'onda a confronto |

---

### Fase 2 — Envelope Generators

| | |
|---|---|
| **Cosimi** | Envelope Generators (pp. 80–87) |
| **Playground** | ADSR, single / multiple trigger, andamento lineare vs esponenziale, reset, loop mode |
| **SoundApp** | **riscrittura da zero** di `Envelope` |
| **Risultato** | l'oscillatore smette di essere un drone e diventa una nota |

> `Envelope` esiste già nel repo ma è **debito non metabolizzato**: va riscritto
> da zero in questa fase, e solo dopo confrontato con l'originale.

---

### Fase 3 — Amplifier (VCA)

| | |
|---|---|
| **Cosimi** | Amplifier (pp. 73–76) |
| **Playground** | gain lineare vs esponenziale, initial / output gain, pan pot, crossfader |
| **SoundApp** | `Amplifier` come stadio di uscita, con pan stereo |
| **Risultato** | la voce minima è completa: oscillatore → envelope → amplificatore |

---

### Fase 4 — Noise Generator

| | |
|---|---|
| **Cosimi** | Noise Generator (pp. 45–47) |
| **Playground** | white noise, pink noise (Voss-McCartney), voltaggi casuali |
| **SoundApp** | `NoiseGenerator` come `AudioSource` |
| **Risultato** | seconda famiglia di sorgenti — prerequisito per hat, clap, snare |

---

### Fase 5 — Drum synthesis

| | |
|---|---|
| **Cosimi** | Oscillatori dedicati (Percussion / Drum Synth Oscillator, p. 39), poi rilettura mirata di Envelope e Noise |
| **Playground** | combinazione dei pezzi già metabolizzati in kick, snare, clap, hi-hat |
| **SoundApp** | **riscrittura da zero** di `KickSynth` + nuovi `SnareSynth`, `ClapSynth`, `HiHatSynth` |
| **Risultato** | primo set di one-shot techno esportabili e usabili davvero |

> Anche `KickSynth` è debito non metabolizzato: stessa regola di `Envelope`.

---

### Fase 6 — Filtri

| | |
|---|---|
| **Cosimi** | Filtri (pp. 49–55) |
| **Playground** | LP / HP / BP / Notch, cutoff, resonance, slope 12 e 24 dB per ottava, keyboard tracking |
| **SoundApp** | `Filter` con parametri modulabili sample-per-sample, collegabile a un envelope |
| **Risultato** | filter sweep su un suono percussivo o su un bass |

---

### Fase 7 — Waveshaping e saturazione

| | |
|---|---|
| **Cosimi** | Trattamenti sulle forme d'onda (pp. 42–44) |
| **Playground** | clipping, symmetry VCA, distortion / overdrive, wave shaping, wave wrapping |
| **SoundApp** | `Waveshaper` applicabile in uscita da qualsiasi `AudioSource` |
| **Risultato** | il carattere sporco e saturo tipico del kick techno |

---

### Fase 8 — Mixer

| | |
|---|---|
| **Cosimi** | Mixer (pp. 64–66) |
| **Playground** | somma pesata di più `AudioSource`, comportamento lineare vs esponenziale, gestione del clipping in somma |
| **SoundApp** | `Mixer` che combina più sorgenti in un buffer unico, con livelli per canale |
| **Risultato** | kick + hat + bass insieme in un solo WAV |

---

### Fase 9 — LFO

| | |
|---|---|
| **Cosimi** | Low Frequency Oscillator (pp. 88–90) |
| **Playground** | waveform, frequency, symmetry / pulse width, delay / fade-in, offset, phase, key sync |
| **SoundApp** | `LFO` come sorgente di modulazione, con routing verso parametri di oscillatori, filtri e amplificatore |
| **Risultato** | movimento automatico: cutoff che respira, tremolo, PWM animata |

---

### Fase 10 — Step Sequencer

| | |
|---|---|
| **Cosimi** | Step Sequencer (pp. 91–95) |
| **Playground** | pattern a lunghezza variabile, controlli di avanzamento, gate buss, modi, clock modulation, swing |
| **SoundApp** | `Sequencer` offline che renderizza un pattern in un loop WAV |
| **Risultato** | un loop di 1–4 battute esportabile, pronto per lo strumento live |

---

### Fase 11 — Controllers (CV processor)

| | |
|---|---|
| **Cosimi** | Sample & Hold (p. 97) + Quantizer (p. 98) |
| **Playground** | S&H triggerato da clock, quantizer su scale musicali |
| **SoundApp** | utility di modulazione: S&H per modulazione stepped, quantizer per vincolare le note a una scala |
| **Risultato** | linee acid e modulazioni casuali ma musicali |

---

### Fase 12 — Voice architecture

| | |
|---|---|
| **Cosimi** | Putting all together (pp. 99–100) |
| **Playground** | logiche di assegnazione voci, comportamenti monofonici e polifonici |
| **SoundApp** | voice manager: prima monofonico, poi polifonico |
| **Risultato** | più note simultanee senza note rubate né click |

---

## Capitoli fuori roadmap

Letture e implementazioni da innestare **solo quando servono davvero**, senza
numero di fase e senza bloccare la progressione.

| Capitolo | Pagine | Quando |
|---|---|---|
| Ancora filtri | 56–60 | Durante o dopo la Fase 6, quando serve *colorare* invece che tagliare: EQ, filtri formanti, comb, phaser, flanger. |
| Trattamenti audio | 77–78 | Materiale del futuro modulo Effects. Il **down sampling** è il più interessante per il techno lo-fi. |
| Ring Modulator | 70–72 | Effetto di nicchia, implementabile come waveshaper evoluto. |
| Filtri multipli | 61–63 | Lettura di cultura (Oberheim, Korg Z1, Nord Lead 3, Andromeda, Voyager, Doepfer). Nessuna implementazione. |
| Vocoder | 67–69 | Richiede FFT e pipeline analysis/synthesis. Fuori scope. |

---

## Stato di avanzamento

**Playground — moduli completati**

- [x] `SampleRateExercises` — 24 test verdi
- [x] `WaveformExercises` — 36 test verdi

**Fasi**

- [ ] Fase 0 — Inquadramento
- [ ] Fase 1 — Oscillatori ← *prossimo passo: lettura Cosimi pp. 28–41*
- [ ] Fase 2 — Envelope Generators
- [ ] Fase 3 — Amplifier
- [ ] Fase 4 — Noise Generator
- [ ] Fase 5 — Drum synthesis
- [ ] Fase 6 — Filtri
- [ ] Fase 7 — Waveshaping e saturazione
- [ ] Fase 8 — Mixer
- [ ] Fase 9 — LFO
- [ ] Fase 10 — Step Sequencer
- [ ] Fase 11 — Controllers
- [ ] Fase 12 — Voice architecture
