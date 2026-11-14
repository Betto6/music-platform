package com.soundapp.music.core.render;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Trasforma una mappa di parametri in testo YAML. È il minimo indispensabile
 * per il dump che accompagna ogni WAV esportato: valori scalari, mappe
 * annidate di UN solo livello, e nient'altro.
 *
 * Scritto a mano, invece di tirare dentro SnakeYAML o Jackson, perché la
 * superficie che ci serve è questa classe: aggiungere una dipendenza (e il suo
 * ciclo di aggiornamenti) per emettere trenta righe di testo non si
 * ripagherebbe. L'output è volutamente a senso unico - lo scriviamo per
 * leggerlo noi, non per rileggerlo dal codice - quindi non ci servono né la
 * copertura completa dello standard YAML né un parser corrispondente.
 */
public class ParameterYamlWriter {

    private static final String INDENT = "  ";

    /**
     * Rende la mappa come documento YAML, una chiave per riga, nell'ordine in
     * cui la mappa la restituisce (per questo i componenti usano LinkedHashMap:
     * l'ordine è parte della leggibilità del file).
     *
     * @throws IllegalArgumentException se un valore annidato contiene a sua
     *         volta una mappa: il formato si ferma a un livello, e fallire
     *         subito è meglio che scrivere un file silenziosamente storto.
     */
    public String toYaml(Map<String, Object> values) {
        StringBuilder yaml = new StringBuilder();

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object value = entry.getValue();

            if (value instanceof Map<?, ?> nested) {
                yaml.append(entry.getKey()).append(":").append(System.lineSeparator());
                appendNested(yaml, entry.getKey(), nested);
            } else {
                yaml.append(entry.getKey())
                        .append(": ")
                        .append(formatScalar(value))
                        .append(System.lineSeparator());
            }
        }

        return yaml.toString();
    }

    private void appendNested(StringBuilder yaml, String parentKey, Map<?, ?> nested) {
        for (Map.Entry<?, ?> entry : nested.entrySet()) {
            Object value = entry.getValue();

            if (value instanceof Map<?, ?>) {
                throw new IllegalArgumentException(
                        "Annidamento troppo profondo per la chiave '" + parentKey + "." + entry.getKey()
                                + "': il writer gestisce un solo livello di mappe annidate");
            }

            yaml.append(INDENT)
                    .append(entry.getKey())
                    .append(": ")
                    .append(formatScalar(value))
                    .append(System.lineSeparator());
        }
    }

    private String formatScalar(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Float || value instanceof Double) {
            return formatDecimal(value);
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        return formatString(value.toString());
    }

    /**
     * Numeri in virgola mobile in forma "piatta" e senza rumore: un
     * {@code 0.3f} viene stampato {@code 0.3} e non {@code 0.30000001192092896},
     * e un valore molto piccolo non diventa notazione esponenziale (che in
     * YAML è legale ma illeggibile accanto agli altri).
     *
     * Il giro passa da {@code toString()} - non da {@code BigDecimal.valueOf(float)} -
     * perché è lui a dare la rappresentazione decimale più corta che
     * riconverte nello stesso float, invece dell'espansione esatta del valore
     * binario. Lo zero finale viene tolto, ma ne resta sempre uno dopo il
     * punto: un parametro in virgola mobile che si stampasse {@code 150} e non
     * {@code 150.0} nasconderebbe la propria natura.
     */
    private String formatDecimal(Object value) {
        BigDecimal decimal = new BigDecimal(value.toString()).stripTrailingZeros();
        String plain = decimal.toPlainString();
        return plain.contains(".") ? plain : plain + ".0";
    }

    /**
     * Le stringhe vanno tra apici doppi solo quando servono davvero, per non
     * sporcare il file: un timestamp ISO o il nome di una classe restano
     * nudi, mentre una stringa vuota, multiriga, o che inizia con un
     * carattere che YAML interpreta (trattini, cancelletti, parentesi...)
     * viene protetta.
     */
    private String formatString(String value) {
        if (needsQuoting(value)) {
            return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\n") + "\"";
        }
        return value;
    }

    private boolean needsQuoting(String value) {
        if (value.isEmpty()) {
            return true;
        }
        if (!value.trim().equals(value)) {
            return true;
        }
        if (value.contains(": ") || value.endsWith(":") || value.contains(" #")
                || value.contains("\n") || value.contains("\"") || value.contains("'")) {
            return true;
        }
        return "#&*!|>%@`[]{},-?".indexOf(value.charAt(0)) >= 0;
    }
}
