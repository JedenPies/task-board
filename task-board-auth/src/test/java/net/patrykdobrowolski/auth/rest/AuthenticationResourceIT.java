package net.patrykdobrowolski.auth.rest;

import net.patrykdobrowolski.auth.db.repository.UsersRepositoryService;
import net.patrykdobrowolski.auth.domain.User;
import net.patrykdobrowolski.auth.rest.dto.AuthenticateCommandDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.Cookie;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthenticationResourceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsersRepositoryService usersRepositoryService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String TEST_USER = "test_user";
    private final String TEST_PASSWORD = "Password123!";

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .login(TEST_USER)
                .passwordEncoded(passwordEncoder.encode(TEST_PASSWORD))
                .build();
        usersRepositoryService.save(user);
    }

    @Test
    void shouldAuthenticateAndReturnCookieWithRefreshToken() throws Exception {
        // given
        AuthenticateCommandDto command = new AuthenticateCommandDto(TEST_USER, TEST_PASSWORD);
        String requestBody = objectMapper.writeValueAsString(command);

        // when
        MvcResult result = mockMvc.perform(post("/api/authentication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().secure("refresh_token", true))
                .andExpect(cookie().path("refresh_token", "/api/authentication/refresh"))
                .andReturn();

        // Dodatkowa weryfikacja ciasteczka (np. czy flaga SameSite jest ustawiona na Strict)
        String setCookieHeader = result.getResponse().getHeader("Set-Cookie");
        assertThat(setCookieHeader).contains("SameSite=Strict");
    }

    @Test
    void shouldFailAuthenticationWhenPasswordIsWrong() throws Exception {
        // given
        AuthenticateCommandDto command = new AuthenticateCommandDto(TEST_USER, "wrong_password");
        String requestBody = objectMapper.writeValueAsString(command);

        // when & then
        mockMvc.perform(post("/api/authentication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                // Jeśli masz GlobalExceptionHandler, który mapuje InvalidCredentialsException na 401 lub 403,
                // upewnij się, że ten status odpowiada Twojej implementacji.
                // Zwykle to .isUnauthorized()
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRefreshTokensUsingCookie() throws Exception {
        // given - Najpierw logujemy się, żeby zdobyć validujący Refresh Token w ciasteczku
        AuthenticateCommandDto command = new AuthenticateCommandDto(TEST_USER, TEST_PASSWORD);
        MvcResult loginResult = mockMvc.perform(post("/api/authentication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshTokenCookie = loginResult.getResponse().getCookie("refresh_token");
        assertThat(refreshTokenCookie).isNotNull();

        // when - Wywołujemy endpoint refresh przekazując pozyskane ciasteczko
        MvcResult refreshResult = mockMvc.perform(post("/api/authentication/refresh")
                        .cookie(refreshTokenCookie))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        // Upewniamy się, że w odpowiedzi dostaliśmy nowe ciasteczko z nowym tokenem
        Cookie newRefreshTokenCookie = refreshResult.getResponse().getCookie("refresh_token");
        assertThat(newRefreshTokenCookie).isNotNull();
        assertThat(newRefreshTokenCookie.getValue()).isNotEqualTo(refreshTokenCookie.getValue());
    }
}