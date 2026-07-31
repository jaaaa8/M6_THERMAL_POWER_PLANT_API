package com.example.m6_thermal_power_plant_api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Text-to-Speech tiếng Việt — làm TRUNG GIAN tải audio từ Google Translate TTS.
 *
 * Vì sao cần backend proxy: gọi thẳng endpoint translate_tts từ trình duyệt bị
 * Google chặn (bot detection) → audio lỗi, FE phải rơi về giọng máy (tiếng Anh).
 * Gọi từ server (kèm User-Agent) thì Google trả về MP3 tiếng Việt bình thường.
 * FE chỉ cần phát: new Audio('/api/v1/tts?text=...').
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tts")
public class TtsController {

    @GetMapping
    public ResponseEntity<byte[]> tts(@RequestParam String text) {
        // Google translate_tts giới hạn ~200 ký tự cho tham số q
        String clipped = text.length() > 200 ? text.substring(0, 200) : text;
        try {
            String q = URLEncoder.encode(clipped, StandardCharsets.UTF_8);
            String url = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=vi&q=" + q;

            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try (InputStream is = conn.getInputStream()) {
                byte[] audio = is.readAllBytes();
                return ResponseEntity.ok()
                        .contentType(MediaType.valueOf("audio/mpeg"))
                        .body(audio);
            }
        } catch (Exception e) {
            log.warn("Tải TTS tiếng Việt thất bại cho text='{}'", clipped, e);
            return ResponseEntity.status(502).build();
        }
    }
}
