package ru.hits.user_service.controller.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import ru.hits.user_service.dto.request.CreateUserRequest;
import ru.hits.user_service.dto.response.UserResponse;
import ru.hits.user_service.handler.command.UserCommandHandler;
import ru.hits.user_service.service.IdempotencyService;
import ru.hits.user_service.service.IdempotencyScopeResolver;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Commands")
public class UserCommandController {

    private final UserCommandHandler commandHandler;
    private final IdempotencyService idempotencyService;
    private final IdempotencyScopeResolver scopeResolver;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать пользователя")
    public UserResponse createUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody @Valid CreateUserRequest command,
            HttpServletRequest request
    ) {
        return idempotencyService.execute(
            scopeResolver.resolveUserScope(jwt, "anonymous"),
                idempotencyKey,
                request.getMethod(),
                request.getRequestURI(),
                command,
                UserResponse.class,
                () -> commandHandler.createUser(command)
        );
    }

    @PatchMapping("/{userId}/block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Заблокировать пользователя")
    public void blockUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable UUID userId,
            HttpServletRequest request
    ) {
        idempotencyService.executeVoid(
            scopeResolver.resolveUserScope(jwt, "anonymous"),
                idempotencyKey,
                request.getMethod(),
                request.getRequestURI(),
                userId,
                () -> commandHandler.blockUser(userId)
        );
    }

    @PatchMapping("/{userId}/unblock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Разблокировать пользователя")
    public void unblockUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable UUID userId,
            HttpServletRequest request
    ) {
        idempotencyService.executeVoid(
            scopeResolver.resolveUserScope(jwt, "anonymous"),
                idempotencyKey,
                request.getMethod(),
                request.getRequestURI(),
                userId,
                () -> commandHandler.unblockUser(userId)
        );
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить пользователя")
    public void deleteUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable UUID userId,
            HttpServletRequest request
    ) {
        idempotencyService.executeVoid(
                scopeResolver.resolveUserScope(jwt, "anonymous"),
                idempotencyKey,
                request.getMethod(),
                request.getRequestURI(),
                userId,
                () -> commandHandler.deleteUser(userId)
        );
    }
}
