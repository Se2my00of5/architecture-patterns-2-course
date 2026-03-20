package ru.hits.user_service.controller.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hits.user_service.dto.request.RegisterRequest;
import ru.hits.user_service.handler.command.AuthCommandHandler;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Commands")
public class AuthCommandController {

    private final AuthCommandHandler authCommandHandler;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Регистрация нового пользователя")
    public void register(@RequestBody @Valid RegisterRequest request) {
        authCommandHandler.register(request);
    }
}
