package ru.hits.user_service.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(UserNotFoundException ex) {
        log.warn("Пользователь не найден: {}", ex.getMessage());
        return buildResponse(ex.getMessage());
    }

    @ExceptionHandler(LoginAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleLoginAlreadyExists(LoginAlreadyExistsException ex) {
        log.warn("Конфликт логина: {}", ex.getMessage());
        return buildResponse(ex.getMessage());
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIdempotencyConflict(IdempotencyConflictException ex) {
        log.warn("Idempotency conflict: {}", ex.getMessage());
        return buildResponse(ex.getMessage());
    }

    @ExceptionHandler(SimulatedServiceFailureException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleSimulatedFailure(SimulatedServiceFailureException ex) {
        log.warn("Simulated service failure: {}", ex.getMessage());
        return buildResponse(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Ошибка валидации: {}", message);
        return buildResponse(message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnreadableMessage(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidFormatException
                && UUID.class.equals(invalidFormatException.getTargetType())) {
            String fieldName = invalidFormatException.getPath().isEmpty()
                    ? "uuid"
                    : invalidFormatException.getPath().get(0).getFieldName();
            String invalidValue = String.valueOf(invalidFormatException.getValue());
            String message = "Некорректный UUID в поле '" + fieldName + "': " + invalidValue;
            log.debug("Invalid UUID in request body: {}", invalidValue);
            return buildResponse(message);
        }

        log.debug("Malformed JSON request: {}", ex.getMessage());
        return buildResponse("Некорректный JSON в теле запроса");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        if (UUID.class.equals(ex.getRequiredType())) {
            String message = "Некорректный UUID в параметре '" + ex.getName() + "': " + ex.getValue();
            log.debug("Invalid UUID in request parameter: {}={}", ex.getName(), ex.getValue());
            return buildResponse(message);
        }

        return buildResponse("Некорректный параметр запроса");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneral(Exception ex) {
        log.error("Внутренняя ошибка сервера", ex);
        return buildResponse("Внутренняя ошибка сервера");
    }

    private ErrorResponse buildResponse(String message) {
        ErrorResponse error = ErrorResponse.builder()
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return error;
    }
}
