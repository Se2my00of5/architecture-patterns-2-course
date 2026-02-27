package ru.hits.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.hits.user_service.dto.request.CreateUserRequest;
import ru.hits.user_service.dto.response.UserResponse;
import ru.hits.user_service.dto.response.UserShortResponse;
import ru.hits.user_service.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isBlocked", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity toEntity(CreateUserRequest command);

    UserResponse toResponse(UserEntity entity);

    UserShortResponse toShortResponse(UserEntity entity);

}