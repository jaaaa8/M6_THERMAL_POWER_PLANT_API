package com.example.m6_thermal_power_plant_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig — CORS configuration for local development & Postman testing.
 *
 * All origins allowed during development/testing.
 * In production, restrict to actual frontend origin or
 * use a reverse proxy (e.g., Nginx) to serve both on the same domain.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")               // <-- ALL ORIGINS for Postman testing
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // .allowCredentials(true)          // DISABLED: credentials incompatible with allowedOrigins("*")
                .maxAge(3600); // Cache preflight response for 1 hour
    }
}