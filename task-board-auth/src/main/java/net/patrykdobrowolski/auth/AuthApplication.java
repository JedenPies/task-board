package net.patrykdobrowolski.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class AuthApplication {

    static void main() {
        SpringApplication.run(AuthApplication.class);
    }
}
