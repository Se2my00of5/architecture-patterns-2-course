package ru.hits.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hits.user_service.entity.UserEntity;
import ru.hits.user_service.entity.enums.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByLogin(String login);

    List<UserEntity> findAllByRole(UserRole role);

    boolean existsByLogin(String login);

    List<UserEntity> findAllByIdIn(List<UUID> ids);
}
