package ru.hits.user_service.controller.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hits.user_service.dto.request.CreateUserRequest;
import ru.hits.user_service.dto.response.UserResponse;
import ru.hits.user_service.handler.command.UserCommandHandler;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Commands")
public class UserCommandController {

    private final UserCommandHandler commandHandler;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать пользователя")
    public UserResponse createUser(@RequestBody @Valid CreateUserRequest command) {
        return commandHandler.createUser(command);
    }

    @PatchMapping("/{userId}/block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Заблокировать пользователя")
    public void blockUser(@PathVariable UUID userId) {
        commandHandler.blockUser(userId);
    }

    @PatchMapping("/{userId}/unblock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Разблокировать пользователя")
    public void unblockUser(@PathVariable UUID userId) {
        commandHandler.unblockUser(userId);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить пользователя")
    public void deleteUser(@PathVariable UUID userId) {
        commandHandler.deleteUser(userId);
    }
}
