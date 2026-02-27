package ru.hits.user_service.handler.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hits.user_service.dto.response.UserResponse;
import ru.hits.user_service.dto.response.UserShortResponse;
import ru.hits.user_service.entity.UserEntity;
import ru.hits.user_service.entity.enums.UserRole;
import ru.hits.user_service.exception.UserNotFoundException;
import ru.hits.user_service.mapper.UserMapper;
import ru.hits.user_service.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryHandler {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse getUserById(UUID userId) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return userMapper.toResponse(entity);
    }

    public UserResponse getUserByLogin(String login) {
        UserEntity entity = userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException(login));
        return userMapper.toResponse(entity);
    }

    public List<UserShortResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toShortResponse)
                .toList();
    }

    public List<UserShortResponse> getUsersByRole(UserRole role) {
        return userRepository.findAllByRole(role).stream()
                .map(userMapper::toShortResponse)
                .toList();
    }

    public List<UserShortResponse> getUsersByIds(List<UUID> ids) {
        return userRepository.findAllByIdIn(ids).stream()
                .map(userMapper::toShortResponse)
                .toList();
    }
}
