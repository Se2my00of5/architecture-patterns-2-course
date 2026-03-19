package ru.hits.user_service.controller.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hits.user_service.dto.response.UserResponse;
import ru.hits.user_service.handler.query.AuthQueryHandler;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Queries")
public class AuthQueryController {

    private final AuthQueryHandler authQueryHandler;

    @GetMapping("/me")
    @Operation(summary = "Получить информацию о текущем пользователе")
    public UserResponse getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return authQueryHandler.getCurrentUserByJwt(jwt);
    }
}
