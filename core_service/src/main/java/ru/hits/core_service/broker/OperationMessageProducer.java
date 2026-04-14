package ru.hits.core_service.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import ru.hits.core_service.broker.message.OperationMessage;
import ru.hits.core_service.exception.BusinessException;
import ru.hits.core_service.exception.IntegrationUnavailableException;
import ru.hits.core_service.integration.IntegrationCircuitBreaker;

import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperationMessageProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final IntegrationCircuitBreaker circuitBreaker;

    @org.springframework.beans.factory.annotation.Value("${app.kafka.topics.account-operations}")
    private String accountOperationsTopic;

    @Retryable(
            retryFor = IllegalStateException.class,
            maxAttemptsExpression = "${integration.resilience.retry.max-attempts:3}",
            backoff = @Backoff(
                delayExpression = "${integration.resilience.retry.delay-ms:300}",
                multiplierExpression = "${integration.resilience.retry.multiplier:2.0}"
            )
    )
    public void send(OperationMessage message) {
        circuitBreaker.executeKafka("account-operations", () -> {
            try {
                String payload = objectMapper.writeValueAsString(message);
                kafkaTemplate.send(accountOperationsTopic, message.getSourceAccountId().toString(), payload).get();
                return null;
            } catch (JsonProcessingException e) {
                throw new BusinessException("Не удалось сериализовать сообщение операции");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException("Операция прервана при отправке в брокер сообщений");
            } catch (ExecutionException e) {
                throw new IllegalStateException("Kafka send failed", e);
            }
        });
    }

    @Recover
    public void recover(IllegalStateException ex, OperationMessage message) {
        log.error("Failed to send operation message to Kafka after retries. operationId={}", message.getOperationId(), ex);
        throw new IntegrationUnavailableException("Kafka временно недоступна для постановки операции в очередь");
    }
}
