package ru.hits.user_service.controller.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hits.user_service.dto.request.LoginRequest;
import ru.hits.user_service.dto.request.RegisterRequest;
import ru.hits.user_service.dto.response.AuthResponse;
import ru.hits.user_service.handler.command.AuthCommandHandler;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Commands")
public class AuthCommandController {

    private final AuthCommandHandler authCommandHandler;

    @PostMapping("/login")
    @Operation(summary = "Вход пользователя")
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {
        return authCommandHandler.login(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Регистрация нового пользователя")
    public AuthResponse register(@RequestBody @Valid RegisterRequest request) {
        return authCommandHandler.register(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновить access токен")
    public AuthResponse refresh(@RequestParam("refresh_token") String refreshToken) {
        return authCommandHandler.refresh(refreshToken);
    }
}
