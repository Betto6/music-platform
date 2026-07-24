# Implementation Guide — SoundApp

> Segui questo documento ogni volta che implementi un nuovo componente.
> Non saltare fasi. Non aggiungere complessità finché il ciclo corrente non funziona.

---

## Principio Guida

In ogni momento di confusione, rispondi a questa domanda:

> **"Questo codice serve a descrivere un suono, a descrivere quando suona, o a materializzarlo su disco?"**

| Risposta | Modulo |
|---|---|
| Descrive *come* suona | `audio-engine` |
| Descrive *quando* e *come evolve* nel tempo | `composition` |
| Lo trasforma in file WAV | `render-engine` |

Le dipendenze vanno **sempre verso il basso**. `audio-engine` non importa mai nulla da `composition` o `render-engine`.

---

## Le 5 Fasi di Ogni Componente

### Fase A — Comprendi il Concetto

Prima di aprire l'IDE, rispondi per iscritto (anche solo in un commento):

- Cosa fa questo componente dal punto di vista **fisico o musicale**?
- Quali **parametri** espone all'utente?
- Quale **output** produce? (campioni audio, eventi, metadati?)
- Da quali componenti **dipende**? In quale modulo va?

> **Esempio — KickSynth:**
> Un kick drum sintetico è un oscillatore sinusoidale con un pitch envelope in discesa rapida
> (da ~150 Hz a ~50 Hz in ~80 ms) e un amplitude envelope con attack immediato e decay breve.
> Parametri: pitch iniziale, pitch finale, decay, punch. Output: float[] di campioni audio.
> Dipende da: AudioContext, Envelope. Modulo: audio-engine.

Non procedere alla Fase B finché non riesci a spiegare il componente con parole tue.

---

### Fase B — Definisci l'Interfaccia

Scrivi **solo** la firma pubblica della classe o interfaccia, senza implementazione.
Questo ti forza a pensare a *come viene usata*, non a come funziona dentro.

```java
// Esempio: prima scrivi solo questo
public class KickSynth {

    public void setPitch(float hz)       { }
    public void setDecay(float ms)       { }
    public void setPunch(float amount)   { }
    public void render(float[] buffer)   { }
}
```

Verifica che l'API abbia senso dal punto di vista del chiamante.
Se ti viene difficile capire come la useresti, l'interfaccia è sbagliata.

---

### Fase C — Implementa

Riempi l'implementazione. In questa fase puoi sbagliare liberamente:
l'interfaccia ti protegge dal propagare gli errori agli altri moduli.

Regole durante l'implementazione:

- **Un file aperto alla volta.** Se stai lavorando su `KickSynth.java`, gli unici
  file rilevanti sono `KickSynth.java`, `AudioContext.java` e l'eventuale `Envelope.java`.
- **Il progetto deve sempre compilare.** Dopo ogni modifica significativa, esegui
  `mvn compile`. Non accumulare errori.
- **Nessuna dipendenza verso l'alto.** Se dentro `audio-engine` stai importando
  qualcosa da `composition` o `render-engine`, stai sbagliando direzione.

---

### Fase D — Collega al Sistema

Aggiungi il componente al modulo corretto e verifica la catena di dipendenze Maven.

```
audio-engine
    ↑
composition
    ↑
render-engine
    ↑
app-starter
```

Se il tuo componente sta in `audio-engine`, non toccare i `pom.xml` degli altri moduli.
Se sta in `composition`, aggiungi al massimo una dipendenza da `audio-engine`.

---

### Fase E — Verifica con un WAV

Il test finale è sempre lo stesso: **esporta un WAV e ascoltalo.**

Non scrivere unit test formali prima di aver verificato con l'orecchio.
Il tuo orecchio è lo strumento di verifica più veloce per il DSP.

```java
// Template di test minimo — mettilo in app-starter o in un main temporaneo
public static void main(String[] args) throws Exception {
    // 1. Istanzia il componente
    KickSynth kick = new KickSynth();
    kick.setPitch(55);
    kick.setDecay(250);

    // 2. Renderizza
    WavRenderer renderer = new WavRenderer();
    renderer.render(kick, 1.0f, Path.of("test_kick.wav"));

    // 3. Ascolta test_kick.wav
    // Se suona bene → il componente è pronto
    // Se non suona bene → torna alla Fase C
}
```

---

## Checklist di Completamento

Prima di considerare un componente "fatto" e passare al successivo:

- [ ] Ho capito il concetto audio/DSP senza guardare codice (Fase A)
- [ ] L'interfaccia pubblica è definita e ha senso dal punto di vista del chiamante (Fase B)
- [ ] L'implementazione compila senza errori (`mvn compile` verde) (Fase C)
- [ ] Le dipendenze Maven vanno nella direzione corretta (Fase D)
- [ ] Ho esportato un WAV e l'ho ascoltato (Fase E)
- [ ] Il WAV suona come mi aspettavo

Se una voce non è spuntata, non si passa al componente successivo.

---

## Roadmap dei Componenti

Segui questo ordine. Ogni riga sblocca quella successiva.

```
[ ] WavRenderer (base)          → sblocca tutto il testing
[ ] Envelope (ADSR)             → sblocca KickSynth e tutti i synth
[ ] KickSynth                   → primo synth completo
[ ] Filter (passa-basso)        → caratterizza il suono
[ ] LFO                         → modulazione automatica dei parametri
[ ] PatternGenerator (base)     → descrive sequenze temporali
[ ] Composition API             → combina più synth in un'unica scena
[ ] Effects chain               → delay, reverb applicati al mix finale
[ ] MIDI / OSC                  → integrazione esterna (opzionale)
```

Non aggiungere un componente dalla lista finché quello precedente
non ha passato tutta la checklist.

---

## Errori Comuni da Evitare

**Iniziare dal componente più interessante, non da quello più necessario.**
Il renderer WAV è noioso ma sblocca tutto. Fallo per primo.

**Tenere troppi file aperti.**
Più file aperti = più contesto da gestire mentalmente = più errori.

**Fare refactoring prima che il ciclo base funzioni.**
Il codice brutto che produce un WAV corretto è meglio del codice elegante
che non produce nulla.

**Aggiungere dipendenze verso l'alto.**
Se `audio-engine` inizia a importare da `composition`, l'architettura
è compromessa e diventa sempre più difficile da correggere.

**Verificare con unit test prima dell'orecchio.**
Per il DSP, un test che passa ma suona storto non vale nulla.
Prima l'orecchio, poi eventualmente il test automatico.
