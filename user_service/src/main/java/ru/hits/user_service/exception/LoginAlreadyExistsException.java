package ru.hits.user_service.exception;

public class LoginAlreadyExistsException extends RuntimeException {

    public LoginAlreadyExistsException(String login) {
        super("Пользователь с логином " + login + " уже существует");
    }
}
