package ru.hits.core_service.mapper;

import org.mapstruct.Mapper;
import ru.hits.core_service.dto.response.AccountResponse;
import ru.hits.core_service.entity.AccountEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountResponse toResponse(AccountEntity entity);

    List<AccountResponse> toResponseList(List<AccountEntity> entities);
}
