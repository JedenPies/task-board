package net.patrykdobrowolski.auth.rest;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.auth.domain.AuthenticationRequest;
import net.patrykdobrowolski.auth.domain.TokensPair;
import net.patrykdobrowolski.auth.rest.dto.AuthenticationRequestDto;
import net.patrykdobrowolski.auth.rest.dto.AuthenticationResultDto;
import net.patrykdobrowolski.auth.rest.mapper.DtoMapper;
import net.patrykdobrowolski.auth.service.AuthenticationService;
import net.patrykdobrowolski.auth.service.InvalidCredentialsException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authentication")
@RequiredArgsConstructor
public class AuthenticationResource {

    private static final int SEVEN_DAYS = 7 * 24 * 60 * 60;

    private final AuthenticationService authenticationService;
    private final DtoMapper dtoMapper;

    @PostMapping
    public AuthenticationResultDto authenticate(
            @RequestBody AuthenticationRequestDto authenticationRequestDto,
            HttpServletResponse rawResponse) throws InvalidCredentialsException {
        AuthenticationRequest request = dtoMapper.toRequest(authenticationRequestDto);
        TokensPair result = authenticationService.authenticate(request);
        rawResponse.addHeader(HttpHeaders.SET_COOKIE, generateCookie(result).toString());
        return AuthenticationResultDto.builder().accessToken(result.accessToken()).build();
    }

    private static @NonNull ResponseCookie generateCookie(TokensPair result) {
        return ResponseCookie.from("refresh_token", result.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/api/authentication/refresh")
                .maxAge(SEVEN_DAYS)
                .sameSite("Strict")
                .build();
    }
}
