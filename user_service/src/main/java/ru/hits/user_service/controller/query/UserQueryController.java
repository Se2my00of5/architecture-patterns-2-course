package ru.hits.user_service.controller.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.hits.user_service.dto.response.UserResponse;
import ru.hits.user_service.dto.response.UserShortResponse;
import ru.hits.user_service.entity.enums.UserRole;
import ru.hits.user_service.handler.query.UserQueryHandler;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Queries")
public class UserQueryController {

    private final UserQueryHandler queryHandler;

    @GetMapping("/{userId}")
    @Operation(summary = "Получить пользователя по ID")
    public UserResponse getUserById(@PathVariable UUID userId) {
        return queryHandler.getUserById(userId);
    }

    @GetMapping("/login/{login}")
    @Operation(summary = "Войти / найти по логину")
    public UserResponse getUserByLogin(@PathVariable String login) {
        return queryHandler.getUserByLogin(login);
    }

    @GetMapping
    @Operation(summary = "Получить всех пользователей")
    public List<UserShortResponse> getAllUsers() {
        return queryHandler.getAllUsers();
    }

    @GetMapping("/by-role")
    @Operation(summary = "Получить пользователей по роли")
    public List<UserShortResponse> getUsersByRole(@RequestParam UserRole role) {
        return queryHandler.getUsersByRole(role);
    }

    @PostMapping("/by-ids")
    @Operation(summary = "Получить пользователей по списку ID")
    public List<UserShortResponse> getUsersByIds(@RequestBody List<UUID> ids) {
        return queryHandler.getUsersByIds(ids);
    }
}
