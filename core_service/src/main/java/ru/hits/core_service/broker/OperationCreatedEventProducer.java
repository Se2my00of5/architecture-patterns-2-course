package ru.hits.core_service.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import ru.hits.core_service.broker.message.OperationCreatedMessage;
import ru.hits.core_service.entity.OperationEntity;
import ru.hits.core_service.integration.IntegrationCircuitBreaker;
import ru.hits.core_service.mapper.OperationMapper;

import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperationCreatedEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final OperationMapper operationMapper;
    private final IntegrationCircuitBreaker circuitBreaker;

    @Value("${app.kafka.topics.operation-created}")
    private String operationCreatedTopic;

    @Retryable(
            retryFor = IllegalStateException.class,
            maxAttemptsExpression = "${integration.resilience.retry.max-attempts:3}",
            backoff = @Backoff(
                delayExpression = "${integration.resilience.retry.delay-ms:300}",
                multiplierExpression = "${integration.resilience.retry.multiplier:2.0}"
            )
    )
    public void send(OperationEntity operation) {
        circuitBreaker.executeKafka("operation-created", () -> {
            var response = operationMapper.toResponse(operation);

            OperationCreatedMessage message = OperationCreatedMessage.builder()
                    .id(response.getId())
                    .accountId(response.getAccountId())
                    .creditId(response.getCreditId())
                    .userId(operation.getAccount().getUserId())
                    .type(response.getType())
                    .amount(response.getAmount())
                    .createdAt(response.getCreatedAt())
                    .description(response.getDescription())
                    .build();

            try {
                String payload = objectMapper.writeValueAsString(message);
                kafkaTemplate.send(operationCreatedTopic, message.getUserId().toString(), payload).get();
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize operation-created message for operationId={}", operation.getId(), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while publishing operation-created event for operationId={}", operation.getId(), e);
            } catch (ExecutionException e) {
                throw new IllegalStateException("Kafka publish failed", e);
            }

            return null;
        });
    }

    @Recover
    public void recover(IllegalStateException ex, OperationEntity operation) {
        log.error("Failed to publish operation-created event after retries for operationId={}", operation.getId(), ex);
    }
}
