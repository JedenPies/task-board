package net.patrykdobrowolski.task_board.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Zezwalaj na wszystkie endpointy w aplikacji
                        // ng serve (4200) oraz frontend z kontenera nginx (port 80 -> Origin "http://localhost").
                        // allowedOriginPatterns zamiast allowedOrigins, bo przy allowCredentials(true) wildcard w allowedOrigins jest niedozwolony.
                        .allowedOriginPatterns("http://localhost:4200", "http://localhost", "http://localhost:*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Wyraźnie dodajemy PUT oraz OPTIONS!
                        .allowedHeaders("*") // Akceptuj wszystkie nagłówki (Content-Type itp.)
                        .allowCredentials(true);
            }
        };
    }
}
