package net.patrykdobrowolski.task_board.rest;

import net.patrykdobrowolski.task_board.domain.exception.ObjectAlreadyExistsException;
import net.patrykdobrowolski.task_board.domain.exception.ObjectNotFoundException;
import net.patrykdobrowolski.task_board.rest.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.text.MessageFormat;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDto onObjectNotFoundException(ObjectNotFoundException exception) {
        return ErrorDto.builder().message(MessageFormat.format(
                "Object {0} with id {1} not found",
                exception.getObjectType(), exception.getObjectId())).build();
    }

    @ExceptionHandler(ObjectAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorDto onObjectAlreadyExistsException(ObjectAlreadyExistsException exception) {
        return ErrorDto.builder().message(MessageFormat.format(
                "Object {0} with id {1} already exists",
                exception.getObjectType(), exception.getObjectId())).build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDto onValidationException(MethodArgumentNotValidException exception) {
        ErrorDto.ErrorDtoBuilder builder = ErrorDto.builder();
        exception.getBindingResult().getAllErrors().stream()
                .filter(e -> e instanceof FieldError)
                .map(e -> (FieldError) e).forEach((error) -> {
                    String fieldName = error.getField();
                    String message = error.getDefaultMessage();
                    String detailMessage = MessageFormat.format("Invalid {0}: {1}", fieldName, message);
                    builder.detail(ErrorDto.Detail.builder().message(detailMessage).build());
                });
        return builder.message("Validation exception").build();
    }
}
