package ru.hits.user_service.handler.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hits.user_service.dto.request.RegisterRequest;
import ru.hits.user_service.entity.UserEntity;
import ru.hits.user_service.exception.LoginAlreadyExistsException;
import ru.hits.user_service.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthCommandHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
}
