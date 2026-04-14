package ru.hits.notification_service.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.hits.notification_service.broker.message.OperationCreatedEvent;
import ru.hits.notification_service.service.FirebaseTopicService;
import ru.hits.notification_service.service.TopicResolver;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperationCreatedConsumer {

    private final ObjectMapper objectMapper;
    private final FirebaseTopicService firebaseTopicService;

    @KafkaListener(topics = "${app.kafka.topics.operation-created}", groupId = "notification-service")
    public void consume(String payload) {
        try {
            OperationCreatedEvent event = objectMapper.readValue(payload, OperationCreatedEvent.class);

            String body = String.format("%s: %s", event.getType(), event.getAmount());
            String operationId = event.getId() == null ? null : event.getId().toString();

            if (event.getUserId() != null) {
                firebaseTopicService.sendToTopic(
                        TopicResolver.clientTopic(event.getUserId()),
                        "Новая операция",
                        body,
                        operationId
                );
            }

            firebaseTopicService.sendToTopic(
                    TopicResolver.employeesTopic(),
                    "Новая операция клиента",
                    body,
                    operationId
            );
        } catch (Exception ex) {
            log.error("Failed to process operation-created event: {}", payload, ex);
        }
    }
}
