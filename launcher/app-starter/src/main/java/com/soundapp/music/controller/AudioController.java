package com.soundapp.music.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.soundapp.music.audio.AudioContext;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/audio")
public class AudioController {

    @Autowired
    private AudioContext audioContext;

    @GetMapping("/info")
    public Map<String, Object> getAudioInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("sampleRate", audioContext.getSampleRate());
        info.put("bufferSize", audioContext.getBufferSize());
        info.put("channels", audioContext.getChannels());
        info.put("latencyMs", audioContext.getLatencyMs());
        info.put("status", "ready");
        return info;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("message", "Audio system is running");
        status.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return status;
    }

    @PostMapping("/test-beep")
    public Map<String, String> testBeep() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Beep played (simulated)");
        return response;
    }
}