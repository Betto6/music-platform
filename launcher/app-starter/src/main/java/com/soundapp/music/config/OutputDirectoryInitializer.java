package com.soundapp.music.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * All'avvio prepara la cartella di export e verifica che sia scrivibile.
 *
 * Esiste per far fallire l'applicazione subito, con un messaggio chiaro, se
 * quella cartella non è utilizzabile. Senza questo controllo l'errore
 * arriverebbe al primo render - cioè potenzialmente molto dopo l'avvio, a
 * lavoro già fatto e con un buffer audio pronto da scrivere e nessun posto
 * dove metterlo. In un container è il caso tipico: il volume non è montato,
 * oppure è montato ma appartiene a un altro utente.
 *
 * Logga anche il percorso assoluto risolto: con un percorso relativo come
 * "out" sapere da quale directory di lavoro è stato risolto è la differenza
 * tra trovare i WAV e cercarli.
 */
@Component
public class OutputDirectoryInitializer {

    private static final Logger log = LoggerFactory.getLogger(OutputDirectoryInitializer.class);

    private final RenderProperties properties;

    public OutputDirectoryInitializer(RenderProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void prepareOutputDirectory() throws IOException {
        Path outputDirectory = properties.outputDirectory().toAbsolutePath().normalize();

        Files.createDirectories(outputDirectory);

        if (!Files.isWritable(outputDirectory)) {
            throw new IllegalStateException(
                    "La cartella di export non è scrivibile: " + outputDirectory
                            + " - controlla i permessi, o il volume montato se sei in container");
        }

        log.info("Cartella di export dei WAV: {}", outputDirectory);
    }
}
