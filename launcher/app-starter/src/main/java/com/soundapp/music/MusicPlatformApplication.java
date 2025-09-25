package com.soundapp.music;

import com.soundapp.music.audio.AudioContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class MusicPlatformApplication {

    public static void main(String[] args) {
        System.out.println("🎵 Starting Music Platform...");

        ConfigurableApplicationContext context = SpringApplication.run(MusicPlatformApplication.class, args);
        try {
            AudioContext audioContext = context.getBean(AudioContext.class);
            System.out.println("✅ Audio System Ready: " + audioContext);
        } catch (Exception e) {
            System.out.println("⚠️ AudioContext non trovato: " + e.getMessage());
        }

        System.out.println("📡 REST API disponibile su http://localhost:8080");
        System.out.println("   - GET  /api/audio/health");
        System.out.println("   - GET  /api/audio/info");
        System.out.println("   - POST /api/audio/test-beep");
    }
}