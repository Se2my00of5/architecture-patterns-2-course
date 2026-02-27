package ru.hits.core_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.hits.core_service.dto.response.OperationResponse;
import ru.hits.core_service.entity.OperationEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OperationMapper {

    @Mapping(source = "account.id", target = "accountId")
    OperationResponse toResponse(OperationEntity entity);

    List<OperationResponse> toResponseList(List<OperationEntity> entities);
}
