package net.patrykdobrowolski.auth.rest;

import net.patrykdobrowolski.auth.service.InvalidCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(value = org.springframework.http.HttpStatus.UNAUTHORIZED)
    public void onInvalidCredentialsException(InvalidCredentialsException e) {
        // TODO do some logging etc.
    }
}
