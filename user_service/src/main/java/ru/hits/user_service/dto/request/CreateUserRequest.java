package ru.hits.user_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.hits.user_service.entity.enums.UserRole;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Команда создания пользователя")
public class CreateUserRequest {

    @NotBlank(message = "Логин обязателен")
    @Schema(description = "Логин пользователя", example = "ivan_ivanov")
    private String login;

    @NotBlank(message = "Пароль обязателен")
    @Schema(description = "Пароль пользователя", example = "password123")
    private String password;

    @NotBlank(message = "ФИО обязательно")
    @Schema(description = "Полное имя пользователя", example = "Иванов Иван Иванович")
    private String fullName;

    @NotEmpty(message = "Должна быть хотя бы одна роль")
    @Schema(description = "Роли пользователя", example = "[CLIENT, EMPLOYEE]")
    private Set<UserRole> roles;
}
