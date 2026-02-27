package ru.hits.user_service.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID userId) {
        super("Пользователь с id " + userId + " не найден");
    }

    public UserNotFoundException(String login) {
        super("Пользователь с логином " + login + " не найден");
    }
}
