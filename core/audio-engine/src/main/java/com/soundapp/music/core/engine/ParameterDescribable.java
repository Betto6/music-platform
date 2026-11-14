package com.soundapp.music.core.engine;

import java.util.Map;

/**
 * Lo implementa un componente che sa dichiarare i parametri con cui sta
 * producendo suono, in forma di coppie chiave/valore leggibili da un umano.
 *
 * Serve a un solo scopo, per ora: quando esportiamo un WAV vogliamo scrivergli
 * accanto un file che documenti COME quel suono è stato generato, così da poter
 * riaprire l'export tra mesi e sapere esattamente da dove ripartire. Il contratto
 * è volutamente a senso unico - si descrive, non si ri-applica: il vocabolario dei
 * parametri cambia ad ogni nuovo componente, e un parser da tenere in pari con
 * quelle modifiche costerebbe più di quanto renda.
 *
 * Vive in audio-engine, e non nel modulo che scrive i file, perché è qui che
 * stanno i componenti che la implementano: le dipendenze del progetto vanno
 * sempre verso il basso (audio-engine non conosce chi lo usa).
 *
 * Convenzioni per chi la implementa:
 * <ul>
 *   <li>le chiavi usano lo stesso vocabolario dei metodi pubblici del componente
 *       ({@code startFrequency}, {@code pitchDecay}, {@code attack}...), così il
 *       file risulta leggibile mettendolo accanto al codice;</li>
 *   <li>i valori sono numeri, stringhe, oppure una mappa annidata (un solo
 *       livello) per descrivere un sotto-componente, es. gli envelope interni
 *       di un synth;</li>
 *   <li>la mappa restituita ha un ordine stabile (es. {@link java.util.LinkedHashMap}),
 *       perché viene stampata su file: l'ordine delle chiavi è parte della
 *       leggibilità.</li>
 * </ul>
 */
public interface ParameterDescribable {

    /**
     * Fotografia dei parametri correnti del componente. È una copia: chi la
     * riceve può leggerla e stamparla, non modificarne il mittente.
     */
    Map<String, Object> describeParameters();
}
