package ru.hits.core_service.exception;

public class IntegrationUnavailableException extends RuntimeException {

    public IntegrationUnavailableException(String message) {
        super(message);
    }
}