package ru.hits.user_service.handler.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hits.user_service.dto.request.LoginRequest;
import ru.hits.user_service.dto.request.RegisterRequest;
import ru.hits.user_service.dto.response.AuthResponse;
import ru.hits.user_service.entity.UserEntity;
import ru.hits.user_service.exception.LoginAlreadyExistsException;
import ru.hits.user_service.exception.UserNotFoundException;
import ru.hits.user_service.repository.UserRepository;
import ru.hits.user_service.service.JwtTokenService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthCommandHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Неверный пароль");
        }

        if (user.getIsBlocked()) {
            throw new IllegalStateException("Пользователь заблокирован");
        }

        log.debug("Пользователь '{}' успешно аутентифицирован", request.getLogin());
        return jwtTokenService.generateTokens(user);
    }

    public AuthResponse register(RegisterRequest request) {
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
        return jwtTokenService.generateTokens(user);
    }

    public AuthResponse refresh(String refreshToken) {
        try {
            var jwt = jwtTokenService.decodeRefreshToken(refreshToken);
            String login = jwt.getClaimAsString("login");
            
            UserEntity user = userRepository.findByLogin(login)
                    .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

            if (user.getIsBlocked()) {
                throw new IllegalStateException("Пользователь заблокирован");
            }

            log.debug("Токен обновлён для пользователя '{}'", login);
            return jwtTokenService.generateTokens(user);
        } catch (Exception e) {
            log.error("Ошибка при обновлении токена", e);
            throw new IllegalArgumentException("Неверный refresh токен", e);
        }
    }
}
