package ru.hits.user_service.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Команда обновления данных пользователя")
public class UpdateUserCommand {

    @Schema(description = "Логин пользователя", example = "new_login")
    private String login;

    @Schema(description = "Полное имя пользователя", example = "Петров Пётр Петрович")
    private String fullName;
}
