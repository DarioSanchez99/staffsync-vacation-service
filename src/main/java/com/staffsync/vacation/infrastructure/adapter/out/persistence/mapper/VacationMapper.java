package com.staffsync.vacation.infrastructure.adapter.out.persistence.mapper;

import com.staffsync.vacation.domain.model.VacationRequest;
import com.staffsync.vacation.infrastructure.adapter.out.persistence.entity.VacationRequestEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VacationMapper {

    VacationRequestEntity toEntity(VacationRequest request);

    VacationRequest toDomain(VacationRequestEntity entity);
}
