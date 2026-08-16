package net.patrykdobrowolski.auth.rest;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.auth.domain.AuthenticateWithExternalProviderCommand;
import net.patrykdobrowolski.auth.domain.AuthenticateWithPasswordCommand;
import net.patrykdobrowolski.auth.domain.exception.InvalidRefreshTokenException;
import net.patrykdobrowolski.auth.domain.AuthenticationResult;
import net.patrykdobrowolski.auth.domain.port.in.AuthenticationUseCase;
import net.patrykdobrowolski.auth.rest.dto.AuthenticateCommandDto;
import net.patrykdobrowolski.auth.rest.dto.AuthenticationResultDto;
import net.patrykdobrowolski.auth.rest.dto.OAuth2LoginRequestDto;
import net.patrykdobrowolski.auth.rest.mapper.DtoMapper;
import net.patrykdobrowolski.auth.domain.AuthProvider;
import net.patrykdobrowolski.auth.domain.exception.InvalidCredentialsException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Stream;

@RestController
@RequestMapping("/api/authentication")
@RequiredArgsConstructor
public class AuthenticationResource {

    private static final int SEVEN_DAYS = 7 * 24 * 60 * 60;

    private final AuthenticationUseCase authenticationUseCase;
    private final DtoMapper dtoMapper;

    @PostMapping
    public AuthenticationResultDto authenticate(
            @RequestBody AuthenticateCommandDto authenticateCommandDto,
            HttpServletResponse rawResponse) throws InvalidCredentialsException {

        AuthenticateWithPasswordCommand request = dtoMapper.toRequest(authenticateCommandDto);
        AuthenticationResult result = authenticationUseCase.authenticate(request);
        rawResponse.addHeader(HttpHeaders.SET_COOKIE, generateCookie(result.refreshToken(), SEVEN_DAYS).toString());
        return map(result);
    }

    @PostMapping("/oauth2/{provider}")
    public AuthenticationResultDto oauthAuthentication(
            @PathVariable AuthProvider provider,
            @RequestBody OAuth2LoginRequestDto request,
            HttpServletResponse rawResponse) throws Exception {
        AuthenticateWithExternalProviderCommand command =
                AuthenticateWithExternalProviderCommand.builder().provider(provider).token(request.getToken()).build();
        AuthenticationResult result = authenticationUseCase.authenticate(command);
        rawResponse.addHeader(HttpHeaders.SET_COOKIE, generateCookie(result.refreshToken(), SEVEN_DAYS).toString());
        return map(result);
    }

    @PostMapping("/refresh")
    public AuthenticationResultDto refresh(HttpServletRequest rawRequest, HttpServletResponse rawResponse) throws InvalidRefreshTokenException {
        AuthenticationResult result = authenticationUseCase.refresh(extractRefreshTokenCookie(rawRequest));
        rawResponse.addHeader(HttpHeaders.SET_COOKIE, generateCookie(result.refreshToken(), SEVEN_DAYS).toString());
        return map(result);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest rawRequest, HttpServletResponse rawResponse) throws InvalidRefreshTokenException {
        String oldToken = extractRefreshTokenCookie(rawRequest);
        authenticationUseCase.logout(oldToken);
        rawResponse.addHeader(HttpHeaders.SET_COOKIE, generateCookie(oldToken, 0).toString());
    }

    private static @NonNull String extractRefreshTokenCookie(HttpServletRequest rawRequest) throws InvalidRefreshTokenException {
        return Stream.of(rawRequest.getCookies())
                .filter(c -> "refresh_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst().orElseThrow(InvalidRefreshTokenException::new);
    }

    private static @NonNull ResponseCookie generateCookie(String refreshToken, long maxAge) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/api/authentication")
                .maxAge(maxAge)
                .sameSite("Strict")
                .build();
    }

    private AuthenticationResultDto map(AuthenticationResult result) {
        return AuthenticationResultDto.builder()
                .userId(result.user().getId())
                .accessToken(result.accessToken())
                .userDisplayName(result.user().getDisplayName())
                .authProvider(result.user().getAuthProvider())
                .build();
    }
}
