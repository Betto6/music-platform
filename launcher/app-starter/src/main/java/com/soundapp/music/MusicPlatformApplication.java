package com.soundapp.music;

import com.soundapp.music.core.AudioContext;
import com.soundapp.music.core.AudioEngine;
import com.soundapp.music.core.AudioIOManager;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class MusicPlatformApplication {

    public static void main(String[] args) {
        System.out.println("🎵 TECHNO SYNTHESIS - Test Audio Base");
        System.out.println("=====================================");

        // Stampa info sistema
        AudioContext.printAudioInfo();

        // Crea i componenti
        AudioEngine engine = new AudioEngine();
        AudioIOManager ioManager = new AudioIOManager(engine);

        try {
            // Avvia il sistema audio
            System.out.println("\nAvviando sistema audio...");
            ioManager.start();

            // Interfaccia utente semplice
            runInteractiveTest(engine);

        } catch (Exception e) {
            System.err.println("ERRORE: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cleanup
            engine.stop();
            ioManager.stop();
            System.out.println("\nTest terminato. Ciao!");
        }
    }

    /**
     * Interfaccia interattiva per testare l'audio
     */
    private static void runInteractiveTest(AudioEngine engine) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n🎮 COMANDI DISPONIBILI:");
        System.out.println("  1 - Start/Stop beep");
        System.out.println("  2 - Cambia frequenza");
        System.out.println("  3 - Cambia volume");
        System.out.println("  t - Test preset frequenze");
        System.out.println("  q - Quit");
        System.out.println("\nProva a digitare '1' per il primo beep!");

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim().toLowerCase();

            try {
                switch (input) {
                    case "1":
                        toggleBeep(engine);
                        break;

                    case "2":
                        changeFrequency(engine, scanner);
                        break;

                    case "3":
                        changeVolume(engine, scanner);
                        break;

                    case "t":
                        testPresetFrequencies(engine);
                        break;

                    case "q":
                        return;

                    case "help":
                    case "h":
                        printCurrentStatus(engine);
                        break;

                    default:
                        System.out.println("Comando non riconosciuto. Usa 'h' per aiuto.");
                }
            } catch (Exception e) {
                System.err.println("Errore: " + e.getMessage());
            }
        }
    }

    /**
     * Accende/spegne il beep
     */
    private static void toggleBeep(AudioEngine engine) {
        if (engine.isRunning()) {
            engine.stop();
            System.out.println("🔇 Beep fermato");
        } else {
            engine.start();
            System.out.println("🔊 Beep avviato!");
            printCurrentStatus(engine);
        }
    }

    /**
     * Cambia la frequenza
     */
    private static void changeFrequency(AudioEngine engine, Scanner scanner) {
        System.out.print("Nuova frequenza (Hz, 20-20000): ");
        try {
            float freq = Float.parseFloat(scanner.nextLine());
            engine.setFrequency(freq);
            System.out.println("✓ Frequenza impostata a " + engine.getFrequency() + " Hz");
        } catch (NumberFormatException e) {
            System.out.println("❌ Frequenza non valida");
        }
    }

    /**
     * Cambia il volume
     */
    private static void changeVolume(AudioEngine engine, Scanner scanner) {
        System.out.print("Nuovo volume (0.0 - 0.8): ");
        try {
            float vol = Float.parseFloat(scanner.nextLine());
            engine.setVolume(vol);
            System.out.println("✓ Volume impostato a " + (int)(engine.getVolume() * 100) + "%");
        } catch (NumberFormatException e) {
            System.out.println("❌ Volume non valido");
        }
    }

    /**
     * Testa alcune frequenze musicali note
     */
    private static void testPresetFrequencies(AudioEngine engine) {
        float[] frequencies = {
                261.63f,  // C4 (Do)
                293.66f,  // D4 (Re)
                329.63f,  // E4 (Mi)
                349.23f,  // F4 (Fa)
                392.00f,  // G4 (Sol)
                440.00f,  // A4 (La) - Standard tuning
                493.88f   // B4 (Si)
        };

        String[] noteNames = {"C4", "D4", "E4", "F4", "G4", "A4", "B4"};

        boolean wasRunning = engine.isRunning();
        if (!wasRunning) {
            engine.start();
        }

        System.out.println("\n🎼 Test scala maggiore di Do:");

        try {
            for (int i = 0; i < frequencies.length; i++) {
                engine.setFrequency(frequencies[i]);
                System.out.println("Suonando " + noteNames[i] + " (" + frequencies[i] + " Hz)");
                Thread.sleep(800); // 800ms per nota
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (!wasRunning) {
            engine.stop();
        }

        System.out.println("🎵 Test completato!");
    }

    /**
     * Stampa lo stato corrente
     */
    private static void printCurrentStatus(AudioEngine engine) {
        System.out.println("\n📊 STATO CORRENTE:");
        System.out.println("  Running: " + (engine.isRunning() ? "SI" : "NO"));
        System.out.println("  Frequenza: " + engine.getFrequency() + " Hz");
        System.out.println("  Volume: " + (int)(engine.getVolume() * 100) + "%");
    }
}