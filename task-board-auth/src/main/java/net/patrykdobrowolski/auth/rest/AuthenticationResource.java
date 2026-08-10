package net.patrykdobrowolski.auth.rest;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.auth.domain.AuthenticateWithPasswordCommand;
import net.patrykdobrowolski.auth.domain.InvalidRefreshTokenException;
import net.patrykdobrowolski.auth.domain.AuthenticationResult;
import net.patrykdobrowolski.auth.rest.dto.AuthenticateCommandDto;
import net.patrykdobrowolski.auth.rest.dto.AuthenticationResultDto;
import net.patrykdobrowolski.auth.rest.dto.OAuth2LoginRequestDto;
import net.patrykdobrowolski.auth.rest.mapper.DtoMapper;
import net.patrykdobrowolski.auth.service.AuthProvider;
import net.patrykdobrowolski.auth.service.AuthenticationService;
import net.patrykdobrowolski.auth.service.InvalidCredentialsException;
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

    private final AuthenticationService authenticationService;
    private final DtoMapper dtoMapper;

    @PostMapping
    public AuthenticationResultDto authenticate(
            @RequestBody AuthenticateCommandDto authenticateCommandDto,
            HttpServletResponse rawResponse) throws InvalidCredentialsException {

        AuthenticateWithPasswordCommand request = dtoMapper.toRequest(authenticateCommandDto);
        AuthenticationResult result = authenticationService.authenticate(request);
        rawResponse.addHeader(HttpHeaders.SET_COOKIE, generateCookie(result).toString());
        return map(result);
    }

    @PostMapping("/oauth2/{provider}")
    public AuthenticationResultDto oauthAuthentication(
            @PathVariable AuthProvider provider,
            @RequestBody OAuth2LoginRequestDto request,
            HttpServletResponse rawResponse) throws Exception {
        AuthenticationResult result = authenticationService.authenticate(provider, request.getToken());
        rawResponse.addHeader(HttpHeaders.SET_COOKIE, generateCookie(result).toString());
        return map(result);
    }

    @PostMapping("/refresh")
    public AuthenticationResultDto refresh(HttpServletRequest rawRequest, HttpServletResponse rawResponse) throws InvalidRefreshTokenException {
        AuthenticationResult result = authenticationService.refresh(extractRefreshTokenCookie(rawRequest));
        rawResponse.addHeader(HttpHeaders.SET_COOKIE, generateCookie(result).toString());
        return map(result);
    }

    private static @NonNull String extractRefreshTokenCookie(HttpServletRequest rawRequest) throws InvalidRefreshTokenException {
        return Stream.of(rawRequest.getCookies())
                .filter(c -> "refresh_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst().orElseThrow(InvalidRefreshTokenException::new);
    }

    private static @NonNull ResponseCookie generateCookie(AuthenticationResult result) {
        return ResponseCookie.from("refresh_token", result.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/api/authentication/refresh")
                .maxAge(SEVEN_DAYS)
                .sameSite("Strict")
                .build();
    }

    private AuthenticationResultDto map(AuthenticationResult result) {
        return AuthenticationResultDto.builder().accessToken(result.accessToken()).userDisplayName(result.user().getDisplayName()).build();
    }
}
