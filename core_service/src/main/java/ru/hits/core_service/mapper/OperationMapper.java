package ru.hits.core_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.SubclassMapping;
import ru.hits.core_service.dto.response.OperationResponse;
import ru.hits.core_service.entity.LoanOperationEntity;
import ru.hits.core_service.entity.OperationEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OperationMapper {

    @Mapping(source = "account.id", target = "accountId")
    @SubclassMapping(source = LoanOperationEntity.class, target = OperationResponse.class)
    OperationResponse toResponse(OperationEntity entity);

    @Mapping(source = "account.id", target = "accountId")
    OperationResponse toResponse(LoanOperationEntity entity);

    List<OperationResponse> toResponseList(List<OperationEntity> entities);
}
