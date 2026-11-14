package com.soundapp.music.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.nio.file.Path;

/**
 * Configurazione del render, letta da application.yml sotto "music.render".
 *
 * Vive in app-starter e non nei moduli core per la stessa ragione per cui
 * Spring Boot sta solo qui: audio-engine e render-engine sono librerie e non
 * devono sapere da dove arrivano i loro parametri. {@code WavRenderer.render()}
 * continua a ricevere un {@link Path} come argomento - è chi lo chiama, cioè
 * l'applicazione, a decidere quel percorso leggendolo da qui.
 *
 * È un record: il binding avviene via costruttore, quindi i valori sono
 * immutabili dopo l'avvio.
 *
 * @param outputDirectory cartella in cui scrivere i WAV esportati. È il punto
 *                        di integrazione con lo strumento live (una cartella
 *                        condivisa) e in container il mount di un volume.
 */
@ConfigurationProperties(prefix = "music.render")
public record RenderProperties(@DefaultValue("out") Path outputDirectory) {
}
