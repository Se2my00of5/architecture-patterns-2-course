package ru.hits.user_service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.hits.user_service.entity.UserEntity;
import ru.hits.user_service.entity.enums.UserRole;
import ru.hits.user_service.repository.UserRepository;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedUser("client0", "password", "Иванов Иван Иванович", Set.of(UserRole.CLIENT), false);
        seedUser("employee0", "password", "Петров Пётр Петрович", Set.of(UserRole.EMPLOYEE), false);
    }

    private void seedUser(String login, String password, String fullName, Set<UserRole> roles, boolean isBlocked) {
        if (userRepository.existsByLogin(login)) {
            log.debug("Пользователь с логином '{}' уже существует, пропускаем.", login);
            return;
        }

        UserEntity user = UserEntity.builder()
                .login(login)
                .passwordHash(passwordEncoder.encode(password))
                .fullName(fullName)
                .roles(roles)
                .isBlocked(isBlocked)
                .build();

        userRepository.save(user);
        log.debug("Создан пользователь: login='{}', fullName='{}', roles={}", login, fullName, roles);
    }
}
