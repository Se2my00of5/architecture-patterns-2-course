package ru.hits.user_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.hits.user_service.entity.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Полная информация о пользователе")
public class UserResponse {

    @Schema(description = "ID пользователя")
    private UUID id;

    @Schema(description = "Логин")
    private String login;

    @Schema(description = "Полное имя")
    private String fullName;

    @Schema(description = "Роль")
    private UserRole role;

    @Schema(description = "Заблокирован ли пользователь")
    private Boolean isBlocked;

    @Schema(description = "Дата создания")
    private Instant createdAt;

    @Schema(description = "Дата обновления")
    private Instant updatedAt;
}
