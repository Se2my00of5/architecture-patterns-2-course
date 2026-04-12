package ru.hits.user_service.controller.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import ru.hits.user_service.dto.request.LogoutRequest;
import ru.hits.user_service.dto.request.RegisterRequest;
import ru.hits.user_service.handler.command.AuthCommandHandler;
import ru.hits.user_service.service.IdempotencyService;
import ru.hits.user_service.service.IdempotencyScopeResolver;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Commands")
public class AuthCommandController {

    private final AuthCommandHandler authCommandHandler;
    private final IdempotencyService idempotencyService;
    private final IdempotencyScopeResolver scopeResolver;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Регистрация нового пользователя")
    public void register(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody @Valid RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        String scope = "register:" + request.getLogin();
        idempotencyService.executeVoid(
                scope,
                idempotencyKey,
                httpRequest.getMethod(),
                httpRequest.getRequestURI(),
                request,
                () -> authCommandHandler.register(request)
        );
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Выход пользователя и отзыв текущих токенов")
    public void logout(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody(required = false) LogoutRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String scope = scopeResolver.resolveUserScope(jwt, "logout:anonymous");
        idempotencyService.executeVoid(
                scope,
                idempotencyKey,
                httpRequest.getMethod(),
                httpRequest.getRequestURI(),
                request,
                () -> authCommandHandler.logout(jwt, request)
        );

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(httpRequest, httpResponse, authentication);
    }
}
