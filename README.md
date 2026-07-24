# Music Platform

Sintetizzatore programmabile per musica elettronica, orientato al render offline.

## Modello mentale

```
Synth Objects → Pattern Generator → Renderer → WAV files
```

Ogni "oggetto" (oscillatore, kick, bass, filtro...) è autonomo e componibile.
Non c'è un motore real-time da servire: si descrive una composizione in codice
e la si renderizza offline, alla velocità di calcolo della macchina, non a
tempo di orologio.

## Moduli

| Modulo | Ruolo |
|---|---|
| `core/audio-engine` | **Synthesis** — cuore del progetto. Oscillatori band-limited, drum synthesis, filtri, envelope, modulation routing. Qui vive il valore del sintetizzatore. |
| `core/composition` | **Pattern Generator / composition scripting** — descrive come gli synth objects evolvono nel tempo: automazione dei parametri, variazioni, struttura. Sostituisce i concetti di "sequencing" e "live coding" con un'unica API di scripting per comporre programmaticamente, senza istanziare oggetti a mano. |
| `core/render-engine` | **Renderer** — trasforma una composizione in audio e la esporta come file WAV. Include la processing chain (effetti) applicata in fase di render, non real-time. |
| `launcher/app-starter` | Applicazione Spring Boot che assembla i moduli core ed espone il punto di ingresso. |

## Perché offline

Rendere il render offline elimina i vincoli più difficili della JVM per l'audio:

- niente preoccupazioni sul GC durante il render
- niente latenza real-time da rispettare
- si può renderizzare più veloce del real-time (limite = CPU, non wall clock)

L'export WAV è il deliverable principale. MIDI e OSC restano opzionali/futuri.

## Ordine di priorità

1. **Synthesis objects** — kick, bass, oscillatori, filtri: massima qualità DSP
2. **Render engine** — trasforma una descrizione temporale in WAV
3. **Composition API** — come si descrivono pattern e strutture in codice

## Stack tecnico

- Java 25, Spring Boot 4.1
- Maven multi-modulo
