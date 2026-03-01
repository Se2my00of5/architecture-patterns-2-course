package ru.hits.user_service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.hits.user_service.entity.UserEntity;
import ru.hits.user_service.entity.enums.UserRole;
import ru.hits.user_service.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) {
        seedUser("client0", "Иванов Иван Иванович", UserRole.CLIENT);
        seedUser("employee0", "Петров Пётр Петрович", UserRole.EMPLOYEE);
    }

    private void seedUser(String login, String fullName, UserRole role) {
        if (userRepository.existsByLogin(login)) {
            log.debug("Пользователь с логином '{}' уже существует, пропускаем.", login);
            return;
        }
        UserEntity user = UserEntity.builder()
                .login(login)
                .fullName(fullName)
                .role(role)
                .build();
        userRepository.save(user);
        log.debug("Создан пользователь: login='{}', fullName='{}', role={}", login, fullName, role);
    }
}
