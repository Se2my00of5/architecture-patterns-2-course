package ru.hits.core_service.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.hits.core_service.exception.IntegrationUnavailableException;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
public class IntegrationCircuitBreaker {

    private enum State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    private static final class CircuitState {
        private final Deque<Boolean> outcomes = new ArrayDeque<>();
        private State state = State.CLOSED;
        private Instant openUntil = Instant.EPOCH;
    }

    private record CircuitConfig(
            boolean enabled,
            int failureRateThreshold,
            int minimumNumberOfCalls,
            int slidingWindowSize,
            int openStateSeconds
    ) {
    }

    private final Map<String, CircuitState> circuits = new ConcurrentHashMap<>();

    @Value("${integration.resilience.circuit-breaker.enabled:true}")
    private boolean enabled;

    @Value("${integration.resilience.circuit-breaker.failure-rate-threshold:70}")
    private int failureRateThreshold;

    @Value("${integration.resilience.circuit-breaker.minimum-number-of-calls:10}")
    private int minimumNumberOfCalls;

    @Value("${integration.resilience.circuit-breaker.sliding-window-size:20}")
    private int slidingWindowSize;

    @Value("${integration.resilience.circuit-breaker.open-state-seconds:30}")
    private int openStateSeconds;

    private CircuitConfig sharedConfig() {
        return new CircuitConfig(enabled, failureRateThreshold, minimumNumberOfCalls, slidingWindowSize, openStateSeconds);
    }

    public <T> T execute(String dependencyName, Supplier<T> action) {
        return executeWithConfig(
                "integration:" + dependencyName,
                action,
                sharedConfig(),
                "Внешний сервис временно отключён: " + dependencyName
        );
    }

    public <T> T executeKafka(String topicName, Supplier<T> action) {
        return executeWithConfig(
                "kafka:" + topicName,
                action,
                sharedConfig(),
                "Kafka временно недоступна: " + topicName
        );
    }

    private <T> T executeWithConfig(String circuitKey, Supplier<T> action, CircuitConfig config, String openMessage) {
        if (!config.enabled()) {
            return action.get();
        }

        CircuitState circuit = circuits.computeIfAbsent(circuitKey, ignored -> new CircuitState());
        synchronized (circuit) {
            if (circuit.state == State.OPEN) {
                if (Instant.now().isBefore(circuit.openUntil)) {
                    throw new IntegrationUnavailableException(openMessage);
                }
                circuit.state = State.HALF_OPEN;
            }
        }

        try {
            T result = action.get();
            onSuccess(circuit, config);
            return result;
        } catch (RuntimeException ex) {
            onFailure(circuit, config);
            throw ex;
        }
    }

    private void onSuccess(CircuitState circuit, CircuitConfig config) {
        synchronized (circuit) {
            if (circuit.state == State.HALF_OPEN) {
                circuit.state = State.CLOSED;
                circuit.outcomes.clear();
                return;
            }
            addOutcome(circuit, true, config);
        }
    }

    private void onFailure(CircuitState circuit, CircuitConfig config) {
        synchronized (circuit) {
            if (circuit.state == State.HALF_OPEN) {
                openCircuit(circuit, config);
                return;
            }

            addOutcome(circuit, false, config);
            if (circuit.outcomes.size() < Math.max(config.minimumNumberOfCalls(), 1)) {
                return;
            }

            long failures = circuit.outcomes.stream().filter(outcome -> !outcome).count();
            int failureRate = (int) Math.round((failures * 100.0) / circuit.outcomes.size());
            if (failureRate >= config.failureRateThreshold()) {
                openCircuit(circuit, config);
            }
        }
    }

    private void addOutcome(CircuitState circuit, boolean success, CircuitConfig config) {
        circuit.outcomes.addLast(success);
        int maxSize = Math.max(config.slidingWindowSize(), 1);
        while (circuit.outcomes.size() > maxSize) {
            circuit.outcomes.removeFirst();
        }
    }

    private void openCircuit(CircuitState circuit, CircuitConfig config) {
        circuit.state = State.OPEN;
        circuit.openUntil = Instant.now().plusSeconds(Math.max(config.openStateSeconds(), 1));
    }
}