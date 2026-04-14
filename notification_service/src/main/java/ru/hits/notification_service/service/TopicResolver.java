package ru.hits.notification_service.service;

import ru.hits.notification_service.entity.UserRole;

import java.util.UUID;

public final class TopicResolver {

    private TopicResolver() {
    }

    public static String forUser(UserRole role, UUID userId) {
        if (role == UserRole.EMPLOYEE) {
            return employeesTopic();
        }
        return "client_" + userId;
    }

    public static String employeesTopic() {
        return "employees_all";
    }

    public static String clientTopic(UUID userId) {
        return "client_" + userId;
    }
}
