# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Stage 1: build
#
# Compila dentro l'immagine, usando il Maven Wrapper: l'unico requisito è un
# JDK, e la versione di Maven la decide il repo (.mvn/wrapper), non la
# macchina. È questo che rende la build riproducibile altrove.
#
# Il JDK dell'immagine è il secondo pilastro: il wrapper fissa Maven, non la
# versione di Java. Qui la fissa il tag dell'immagine base, e deve combaciare
# con <java.version> nel pom - altrimenti il build fallisce su "release 25".
# ---------------------------------------------------------------------------
FROM eclipse-temurin:25-jdk AS build

WORKDIR /build

# Prima solo il wrapper e i pom: finché le dipendenze non cambiano, Docker
# riusa il layer con il repository Maven già popolato e non riscarica mezzo
# Maven Central a ogni modifica del codice. Per questo i pom vengono copiati
# separatamente dai sorgenti, e non tutti insieme.
COPY mvnw ./
COPY .mvn ./.mvn
COPY pom.xml ./
COPY core/audio-engine/pom.xml core/audio-engine/
COPY core/composition/pom.xml core/composition/
COPY core/render-engine/pom.xml core/render-engine/
COPY launcher/app-starter/pom.xml launcher/app-starter/

# -B = batch mode: nessun output interattivo, nessun colore ANSI nei log.
# I moduli del progetto si escludono: sono SNAPSHOT che non esistono ancora.
RUN ./mvnw -B dependency:go-offline -DexcludeGroupIds=com.soundapp.music

COPY core core
COPY launcher launcher

# I test girano QUI, non solo in CI: sono la prova che il render su file
# funziona in un ambiente senza scheda audio (usano @TempDir e WavRenderer,
# mai AudioPreview). Se un giorno un test pretendesse un device audio, questa
# riga lo scoprirebbe subito.
RUN ./mvnw -B clean package

# Spring Boot sa spacchettare il proprio jar in layer ordinati per frequenza
# di cambiamento: le dipendenze (stabili) separate dal codice applicativo
# (che cambia a ogni commit). Copiandoli come layer distinti nello stage
# finale, un redeploy trasferisce solo i pochi KB che sono cambiati davvero
# invece dei ~9 MB del jar intero.
RUN java -Djarmode=tools -jar launcher/app-starter/target/app-starter-*.jar \
        extract --layers --launcher --destination /extracted

# ---------------------------------------------------------------------------
# Stage 2: runtime
#
# Immagine finale: solo un JRE (non un JDK: niente compilatore in produzione)
# e i layer estratti. Nessun Maven, nessun sorgente, nessun .m2.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:25-jre AS runtime

# Stessa trappola della build su Windows, al contrario: in un'immagine minima
# LANG non è impostata, la JVM deduce un encoding ASCII e ogni accento nei
# messaggi di log (che qui sono in italiano) diventa un punto di domanda.
# C.UTF-8 è il locale UTF-8 che non richiede pacchetti aggiuntivi.
ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8

# Utente non privilegiato: un processo che deve solo generare WAV non ha
# motivo di girare come root dentro il container.
RUN groupadd --system --gid 10001 soundapp \
    && useradd --system --uid 10001 --gid soundapp soundapp

# La cartella di export, dichiarata come volume: è l'unico stato che il
# container produce e l'unica cosa che deve sopravvivergli. Il README
# descrive questa cartella come l'unica integrazione con lo strumento live -
# qui si monta la cartella condivisa dell'host.
ENV MUSIC_RENDER_OUTPUT_DIRECTORY=/var/soundapp/out
RUN mkdir -p /var/soundapp/out && chown -R soundapp:soundapp /var/soundapp
VOLUME /var/soundapp/out

WORKDIR /app

# L'ordine dei COPY segue la stabilità dei layer: dal meno al più volatile.
COPY --from=build --chown=soundapp:soundapp /extracted/dependencies/ ./
COPY --from=build --chown=soundapp:soundapp /extracted/spring-boot-loader/ ./
COPY --from=build --chown=soundapp:soundapp /extracted/snapshot-dependencies/ ./
COPY --from=build --chown=soundapp:soundapp /extracted/application/ ./

USER soundapp

# MaxRAMPercentage invece di un -Xmx fisso: così il limite di memoria lo
# decide il container (docker run -m, o i limiti del pod) e non un numero
# scritto qui dentro, che sarebbe sbagliato su qualunque macchina diversa da
# quella su cui è stato scelto.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", \
            "org.springframework.boot.loader.launch.JarLauncher"]
