# Indice — *Analog & Virtual Analog* (Enrico Cosimi)

> Riferimento teorico primario del progetto.
> Serve per saltare direttamente al capitolo giusto quando una fase della
> [roadmap](ROADMAP.md) lo richiede, senza rileggere il libro in ordine.

---

## PRIMO INQUADRAMENTO — pp. 6–18

**p. 7 — Nozioni preliminari**
- La struttura: sintetizzatore modulare (*virtual modular*), semimodulare, integrato
- La potenza: monofonia, polifonia, politimbricità
- La tecnica: tecnologia analogica, digitale, virtual analog, virtual synth & plug-in

**p. 16 — L'interfaccia utente (panoramica storica)**
- La vecchia scuola analogica
- Le prime macchine polifoniche
- Semplificazione e depauperamento
- Visual editing
- Il ritorno dei potenziometri (…e non solo)
- Strumento "virtuale" e programmer hardware
- Lo stato di fatto

---

## CONCETTI BASE — pp. 19–26

**p. 20 — Il sistema nervoso del sintetizzatore**
- Teoria del control voltage: Control Voltage, Gate, Trigger
  (*cosa succede(va) in una tastiera analogica*, *micro e macro intervalli*)
- Tastiere monofoniche e bifoniche: la conquista della tastiera "polifonica"
- L'interfacciamento MIDI: gestione monofonica nota on/off, gestione dei parametri espressivi

**p. 25 — Cosa c'è dentro un sintetizzatore?**
- Categorie funzionali: le applicazioni, patch charts (gli appunti del sintetizzatore)

---

## SIGNAL GENERATORS — pp. 27–47

**p. 28 — Oscillator**
- Parametri fondamentali:
  - Frequency
  - Waveform (*sine, triangle, sawtooth, square, pulse — Pulse Width Modulation*)
  - Level
  - Synchronization (*hard sync, soft sync*)
  - Frequency Modulation (*sidebands, FM come tecnica di sintesi, FM esponenziale in banda sub audio, FM esponenziale in banda audio, FM lineare*)
- Parametri operativi in breve

**p. 39 — Oscillatori speciali**
- Sampling Oscillator
- Wavetable Oscillator
- Oscillator Driver / Oscillator
- Oscillatori dedicati: Sine Bank, Percussion Oscillator, **Drum Synth Oscillator**, Spectral Oscillator, Formant Oscillator

**p. 42 — Trattamenti sulle forme d'onda**
- Generatori subarmonici e divisione di frequenza
- Waveform processing: *clipping, symmetry VCA, distortion/overdrive, wave shaping, wave wrapping*

**p. 45 — Noise Generator**
- Diversi colori: differenza tra white noise e pink noise
- Generazione di voltaggi casuali
- Noise digitale
- Trattamenti audio per il noise generator: audio divider, vocoding, syncing

---

## AUDIO PROCESSORS — pp. 48–78

**p. 49 — Filtri**
- Filtri statici e filtri dinamici
- Funzionamento del filtro: solo sintesi sottrattiva?, phase shift
- Parametri operativi:
  - Mode (*Low Pass, High Pass, Band Pass, Band Reject / Notch*)
  - Cutoff Frequency
  - Resonance
  - Cutoff Slope
  - Keyboard Tracking
- Audio Frequency Modulation
- Resonance Modulation

**p. 56 — Ancora filtri**
- Equalizzatori: grafici, parametrici, filtri formanti
- Comb filter: phaser, flanger

**p. 61 — Filtri multipli** *(casi di studio hardware)*
- Oberheim Xpander / Matrix 12 · Korg Z1 · Clavia Nord Lead 3
- Alesis A6 Andromeda · Moog Voyager · Doepfer A-107

**p. 64 — Mixer**
- Il mixer nel sintetizzatore: gestione delle sorgenti sonore, gestione dei voltaggi di controllo
- Comportamento esponenziale e lineare
- Integrazione funzionale
- VC-Mixer
- Controlli: Input Level, Ext CV Amount, Mix Out

**p. 67 — Vocoder**
- Funzionamento · Analysis/Synthesis · Slew/Freeze
- Il segnale speech · Il segnale carrier

**p. 70 — Ring Modulator**
- Ancora le sidebands
- Amplitude Modulation vs. Ring Modulation
- AC/DC Coupling

**p. 73 — Amplifier**
- Comportamento lineare ed esponenziale
- Controlli: Amplitude Modulation, Linear/Exponential, Initial Gain, Output Gain
- Controllo dinamico
- Pan Pot / Crossfader

**p. 77 — Trattamenti audio**
- Frequency Shifter · Digitizer / Down Sampling · Chorus · Shaper · Compressor / Expander

---

## CONTROL VOLTAGE GENERATORS — pp. 79–95

**p. 80 — Envelope Generators**
- Le origini: switch trigger
- Definizioni: numero degli stadi, unipolarità e bipolarità, i tempi disponibili
- Andamento lineare, logaritmico ed esponenziale
- Gate e Trigger: single trigger / multiple trigger
- Reset
- Le "forme" dell'inviluppo: inviluppi analogici, inviluppi digitali
- Modi: freerun, loop
- Modulazioni: intervento individuale e globale

**p. 88 — Low Frequency Oscillator**
- Parametri operativi: Waveform, Frequency, Symmetry/Pulse Width, Delay/Fade In, Offset, Phase, Mono/Poly, Key Sync
- Differenziazione funzionale

**p. 91 — Step Sequencer**
- In teoria · In pratica
- Il sequencer parametro per parametro: controlli di avanzamento, Step / Step Number, Row, Gate buss e Gate input, Quantizer / Slew Limiter
- Modi
- Clock Modulation
- Waveform Generator
- Real time transpose: sequencer in serie ed in parallelo
- Sequencer dedicati

---

## I CONTROLLERS: CONTROL VOLTAGE PROCESSOR — pp. 96–100

**p. 97 — Sample & Hold**

**p. 98 — Quantizer**
- Scale selector

**p. 99 — Putting all together**
- Il modo Performance:
  - controllo orizzontale delle timbriche: Split/Layer (*esasperazione dello split mode*)
  - controllo verticale delle timbriche: Velocity Switch / Crossfade
- Logiche di assegnazione voci: comportamenti monofonici, comportamenti polifonici

---

## Appendici

| Pagina | Sezione |
|---|---|
| 101 | Come orientarsi? |
| 102 | Glossario dei termini |
| 111 | Indicazioni bibliografiche |
