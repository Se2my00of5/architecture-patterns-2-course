package ru.hits.user_service.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.hits.user_service.entity.enums.UserRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Команда создания пользователя")
public class CreateUserCommand {

    @NotBlank(message = "Логин обязателен")
    @Schema(description = "Логин пользователя", example = "ivan_ivanov")
    private String login;

    @NotBlank(message = "ФИО обязательно")
    @Schema(description = "Полное имя пользователя", example = "Иванов Иван Иванович")
    private String fullName;

    @NotNull(message = "Роль обязательна")
    @Schema(description = "Роль пользователя", example = "CLIENT")
    private UserRole role;
}
