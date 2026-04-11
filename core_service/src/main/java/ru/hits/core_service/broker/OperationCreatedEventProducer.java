package ru.hits.core_service.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.hits.core_service.broker.message.OperationCreatedMessage;
import ru.hits.core_service.entity.OperationEntity;
import ru.hits.core_service.mapper.OperationMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperationCreatedEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final OperationMapper operationMapper;

    @Value("${app.kafka.topics.operation-created}")
    private String operationCreatedTopic;

    public void send(OperationEntity operation) {
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
            kafkaTemplate.send(operationCreatedTopic, message.getUserId().toString(), payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize operation-created message for operationId={}", operation.getId(), e);
        } catch (Exception e) {
            log.error("Failed to publish operation-created event for operationId={}", operation.getId(), e);
        }
    }
}
