package ru.hits.user_service.handler.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hits.user_service.dto.request.CreateUserRequest;
import ru.hits.user_service.dto.response.UserResponse;
import ru.hits.user_service.entity.UserEntity;
import ru.hits.user_service.exception.LoginAlreadyExistsException;
import ru.hits.user_service.exception.UserNotFoundException;
import ru.hits.user_service.mapper.UserMapper;
import ru.hits.user_service.repository.UserRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandHandler {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse createUser(CreateUserRequest command) {;
        if (userRepository.existsByLogin(command.getLogin())) {
            throw new LoginAlreadyExistsException(command.getLogin());
        }

        UserEntity entity = userMapper.toEntity(command);
        entity.setIsBlocked(false);

        UserEntity saved = userRepository.save(entity);
        
        log.debug("Пользователь создан: id={}, login={}", saved.getId(), saved.getLogin());
        return userMapper.toResponse(saved);
    }

    public void blockUser(UUID userId) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        entity.setIsBlocked(true);

        userRepository.save(entity);

        log.debug("Пользователь заблокирован: id={}", userId);
    }

    public void unblockUser(UUID userId) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        entity.setIsBlocked(false);

        userRepository.save(entity);

        log.debug("Пользователь разблокирован: id={}", userId);
    }

    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        userRepository.deleteById(userId);

        log.debug("Пользователь удалён: id={}", userId);
    }
}
