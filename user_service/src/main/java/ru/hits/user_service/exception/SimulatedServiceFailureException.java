package ru.hits.user_service.exception;

public class SimulatedServiceFailureException extends RuntimeException {

    public SimulatedServiceFailureException(String message) {
        super(message);
    }
}
