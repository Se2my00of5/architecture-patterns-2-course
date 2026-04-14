package ru.hits.notification_service.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class FirebaseTopicService {

    private final FirebaseMessaging firebaseMessaging;
    private final boolean enabled;

    public FirebaseTopicService(
            ObjectProvider<FirebaseMessaging> firebaseMessagingProvider,
            @Value("${firebase.enabled:false}") boolean enabled
    ) {
        this.firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        this.enabled = enabled;
    }

    public void subscribeToken(String token, String topic) {
        if (!isReady()) {
            return;
        }
        try {
            firebaseMessaging.subscribeToTopic(List.of(token), topic);
        } catch (FirebaseMessagingException ex) {
            log.warn("Failed to subscribe token to topic={}, reason={}", topic, ex.getMessage());
        }
    }

    public void unsubscribeToken(String token, String topic) {
        if (!isReady()) {
            return;
        }
        try {
            firebaseMessaging.unsubscribeFromTopic(List.of(token), topic);
        } catch (FirebaseMessagingException ex) {
            log.warn("Failed to unsubscribe token from topic={}, reason={}", topic, ex.getMessage());
        }
    }

    public void sendToTopic(String topic, String title, String body, String operationId) {
        if (!isReady()) {
            return;
        }
        var messageBuilder = com.google.firebase.messaging.Message.builder()
                .setTopic(topic)
                .putData("title", title)
                .putData("body", body);

        if (operationId != null) {
            messageBuilder.putData("operationId", operationId);
        }

        try {
            firebaseMessaging.send(messageBuilder.build());
        } catch (FirebaseMessagingException ex) {
            log.warn("Failed to send topic notification topic={}, reason={}", topic, ex.getMessage());
        }
    }

    private boolean isReady() {
        if (!enabled || firebaseMessaging == null) {
            log.debug("Firebase push is disabled or not configured");
            return false;
        }
        return true;
    }
}
