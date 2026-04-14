package ru.hits.shared_resilience.exception;

public class SimulatedServiceFailureException extends RuntimeException {

    public SimulatedServiceFailureException(String message) {
        super(message);
    }
}
