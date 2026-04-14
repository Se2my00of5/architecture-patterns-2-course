package ru.hits.notification_service.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.hits.shared_resilience.exception.IdempotencyConflictException;
import ru.hits.shared_resilience.exception.SimulatedServiceFailureException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyConflict(IdempotencyConflictException ex) {
        log.warn("Idempotency conflict: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(SimulatedServiceFailureException.class)
    public ResponseEntity<ErrorResponse> handleSimulatedFailure(SimulatedServiceFailureException ex) {
        log.warn("Simulated service failure: {}", ex.getMessage());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.debug("IllegalArgumentException: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.debug("Validation failed: {}", message);
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidFormatException
                && UUID.class.equals(invalidFormatException.getTargetType())) {
            String fieldName = invalidFormatException.getPath().isEmpty()
                    ? "uuid"
                    : invalidFormatException.getPath().get(0).getFieldName();
            String invalidValue = String.valueOf(invalidFormatException.getValue());
            String message = "Некорректный UUID в поле '" + fieldName + "': " + invalidValue;
            log.debug("Invalid UUID in request body: {}", invalidValue);
            return buildResponse(HttpStatus.BAD_REQUEST, message);
        }

        log.debug("Malformed JSON request: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Некорректный JSON в теле запроса");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        if (UUID.class.equals(ex.getRequiredType())) {
            String message = "Некорректный UUID в параметре '" + ex.getName() + "': " + ex.getValue();
            log.debug("Invalid UUID in request parameter: {}={}", ex.getName(), ex.getValue());
            return buildResponse(HttpStatus.BAD_REQUEST, message);
        }

        return buildResponse(HttpStatus.BAD_REQUEST, "Некорректный параметр запроса");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        ErrorResponse error = ErrorResponse.builder()
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(error);
    }
}
