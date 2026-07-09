package net.patrykdobrowolski.auth.rest;

import lombok.extern.slf4j.Slf4j;
import net.patrykdobrowolski.auth.domain.InvalidRefreshTokenException;
import net.patrykdobrowolski.auth.service.InvalidCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(value = org.springframework.http.HttpStatus.UNAUTHORIZED)
    public void onInvalidCredentialsException() {
        log.warn("Invalid credentials");
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    @ResponseStatus(value = org.springframework.http.HttpStatus.UNAUTHORIZED)
    public void onInvalidRefreshTokenException() {
        log.warn("Invalid refresh token");
    }
}
