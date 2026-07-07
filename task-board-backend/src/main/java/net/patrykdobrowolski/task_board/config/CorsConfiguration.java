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
                        .allowedOrigins("http://localhost:4200") // Adres Twojego Angulara
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Wyraźnie dodajemy PUT oraz OPTIONS!
                        .allowedHeaders("*") // Akceptuj wszystkie nagłówki (Content-Type itp.)
                        .allowCredentials(true);
            }
        };
    }
}
