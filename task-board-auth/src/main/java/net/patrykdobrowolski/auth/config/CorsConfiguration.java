package net.patrykdobrowolski.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("local")
public class CorsConfiguration {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("http://localhost:4200", "http://localhost", "http://localhost:*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Wyraźnie dodajemy PUT oraz OPTIONS!
                        .allowedHeaders("*") // Akceptuj wszystkie nagłówki (Content-Type itp.)
                        .allowCredentials(true);
            }
        };
    }
}
