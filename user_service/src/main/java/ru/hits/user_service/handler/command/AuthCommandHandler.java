package ru.hits.user_service.handler.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hits.user_service.dto.request.LogoutRequest;
import ru.hits.user_service.dto.request.RegisterRequest;
import ru.hits.user_service.entity.UserEntity;
import ru.hits.user_service.exception.LoginAlreadyExistsException;
import ru.hits.user_service.repository.UserRepository;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthCommandHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OAuth2AuthorizationService authorizationService;

    public void register(RegisterRequest request) {
        if (userRepository.existsByLogin(request.getLogin())) {
            throw new LoginAlreadyExistsException("Пользователь с таким логином уже существует");
        }

        UserEntity user = UserEntity.builder()
                .login(request.getLogin())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .roles(request.getRoles())
                .isBlocked(false)
                .build();

        userRepository.save(user);

        log.debug("Пользователь '{}' успешно зарегистрирован", request.getLogin());
    }

    public void logout(Jwt jwt, LogoutRequest request) {
        OAuth2Authorization byAccessToken = findByAccessToken(jwt);
        OAuth2Authorization byRefreshToken = findByRefreshToken(request);

        revokeAuthorization(byAccessToken);

        if (byRefreshToken != null && !Objects.equals(byRefreshToken.getId(), byAccessToken != null ? byAccessToken.getId() : null)) {
            revokeAuthorization(byRefreshToken);
        }

        String login = jwt != null ? jwt.getClaimAsString("login") : null;
        if (login == null && jwt != null) {
            login = jwt.getSubject();
        }
        log.debug("Logout completed for user='{}'", login);
    }

    private OAuth2Authorization findByAccessToken(Jwt jwt) {
        if (jwt == null || jwt.getTokenValue() == null || jwt.getTokenValue().isBlank()) {
            return null;
        }
        return authorizationService.findByToken(jwt.getTokenValue(), OAuth2TokenType.ACCESS_TOKEN);
    }

    private OAuth2Authorization findByRefreshToken(LogoutRequest request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return null;
        }
        return authorizationService.findByToken(request.getRefreshToken(), OAuth2TokenType.REFRESH_TOKEN);
    }

    private void revokeAuthorization(OAuth2Authorization authorization) {
        if (authorization != null) {
            authorizationService.remove(authorization);
        }
    }
}
