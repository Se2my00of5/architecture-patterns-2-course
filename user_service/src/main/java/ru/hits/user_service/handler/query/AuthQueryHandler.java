package ru.hits.user_service.handler.query;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.hits.user_service.dto.response.UserResponse;
import ru.hits.user_service.entity.UserEntity;
import ru.hits.user_service.exception.UserNotFoundException;
import ru.hits.user_service.mapper.UserMapper;
import ru.hits.user_service.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthQueryHandler {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse getCurrentUserByJwt(Jwt jwt) {
        String login = jwt.getSubject();
        if (!StringUtils.hasText(login)) {
            login = jwt.getClaimAsString("login");
        }
        if (!StringUtils.hasText(login)) {
            login = jwt.getClaimAsString("preferred_username");
        }

        if (!StringUtils.hasText(login)) {
            throw new UserNotFoundException("В токене отсутствует имя пользователя");
        }

        UserEntity user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        return userMapper.toResponse(user);
    }
}
