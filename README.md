# Music Platform — SoundApp

Sintetizzatore programmabile per musica elettronica, orientato al render offline.

## Modello mentale

```
Synth Objects → Pattern Generator → Renderer → WAV files
```

Ogni "oggetto" (oscillatore, kick, bass, filtro...) è autonomo e componibile.
Non c'è un motore real-time da servire: si descrive una composizione in codice
e la si renderizza offline, alla velocità di calcolo della macchina, non a
tempo di orologio.

## Cos'è, e cosa non è

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
Hot reload su filesystem e comunicazione OSC sono rimandati: reintrodurrebbero
i vincoli real-time che lo scope offline elimina.

## Perché offline

Rendere il render offline elimina i vincoli più difficili della JVM per l'audio:

- niente preoccupazioni sul GC durante il render
- niente latenza real-time da rispettare
- si può renderizzare più veloce del real-time (limite = CPU, non wall clock)

L'export WAV è il deliverable principale. MIDI e OSC restano opzionali/futuri.

## Moduli

| Modulo | Ruolo |
|---|---|
| `core/audio-engine` | **Synthesis** — cuore del progetto. Oscillatori band-limited, drum synthesis, filtri, envelope, modulation routing. Qui vive il valore del sintetizzatore. |
| `core/composition` | **Pattern Generator** — descrive come i synth objects evolvono nel tempo: automazione dei parametri, variazioni, struttura. Un'API di scripting per comporre programmaticamente, senza istanziare oggetti a mano. Non è un'interfaccia di performance: tutto quello che descrive viene materializzato offline dal renderer. |
| `core/render-engine` | **Renderer** — trasforma una composizione in audio e la esporta come file WAV. Include la processing chain (effetti) applicata in fase di render, non real-time. Contiene anche `AudioPreview`, per riascoltare un suono durante il sound design senza passare dal disco. |
| `launcher/app-starter` | Applicazione Spring Boot che assembla i moduli core ed espone il punto di ingresso. |

Le dipendenze vanno **sempre verso il basso**:

```
audio-engine  ←  composition  ←  render-engine  ←  app-starter
```

`audio-engine` non importa mai nulla da `composition` o `render-engine`.

## Metodo di lavoro

Il progetto procede su **due binari separati**:

| Progetto | Ruolo |
|---|---|
| `dsp-exercises` | **Playground.** Un concetto alla volta, isolato, con unit test. Qui si può sbagliare. |
| `soundapp` (questo repo) | **Implementazione.** Si tocca solo dopo che l'esercizio corrispondente è verde. |

**Regola dura, non linea guida:** nessun concetto entra in SoundApp prima di
essere stato metabolizzato con esercizi isolati e test passanti.

## Documenti

| File | Contenuto |
|---|---|
| [`ROADMAP.md`](ROADMAP.md) | Le 12 fasi del progetto, ordinate per catena del segnale e risultato udibile. Per ogni fase: cosa leggere, quali esercizi fare, cosa implementare. |
| [`COSIMI_INDEX.md`](COSIMI_INDEX.md) | Indice di *Analog & Virtual Analog* di Enrico Cosimi, riferimento teorico primario, con page range per saltare al capitolo giusto. |
| [`IMPLEMENTATION_GUIDE.md`](IMPLEMENTATION_GUIDE.md) | Il metodo per implementare un singolo componente: le 5 fasi, la checklist di completamento, gli errori da evitare. |

La priorità corrente e il prossimo passo si leggono in fondo a `ROADMAP.md`.

## Stack tecnico

- Java 25, Spring Boot 4.1
- Maven multi-modulo
- JUnit 5 per i test
- Java Sound API (`javax.sound.sampled`) per export WAV e preview — nessuna dipendenza audio esterna
- Lombok

C++ e JUCE sono la scelta corretta per il real-time e per i plugin VST/AU, e
restano un'opzione per il futuro. Per questo progetto — offline, didattico,
orientato all'export — Java è adeguato.
