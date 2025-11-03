package com.pruebas.unitarias.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Aplica a todos los endpoints
                        .allowedOrigins(
                                "http://localhost:5173",   // Vite (React) en desarrollo
                                "http://localhost:3000",   // (opcional) CRA
                                "https://tu-frontend.com"  // Producción (ajústalo)
                        )
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Location") // (opcional) si devuelves Location u otros
                        .allowCredentials(false)      // Si usas cookies o auth con fetch/axios { withCredentials: true }
                        .maxAge(3600);               // Cachea la preflight 1 hora
            }
        };
    }
}
