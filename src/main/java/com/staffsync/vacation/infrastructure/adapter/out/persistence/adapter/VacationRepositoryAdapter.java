package com.staffsync.vacation.infrastructure.adapter.out.persistence.adapter;

import com.staffsync.vacation.domain.model.VacationRequest;
import com.staffsync.vacation.domain.model.VacationStatus;
import com.staffsync.vacation.domain.port.out.VacationRepository;
import com.staffsync.vacation.infrastructure.adapter.out.persistence.entity.VacationRequestEntity;
import com.staffsync.vacation.infrastructure.adapter.out.persistence.mapper.VacationMapper;
import com.staffsync.vacation.infrastructure.adapter.out.persistence.repository.VacationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VacationRepositoryAdapter implements VacationRepository {

    private final VacationJpaRepository jpaRepository;
    private final VacationMapper mapper;

    @Override
    public VacationRequest save(VacationRequest request) {
        VacationRequestEntity entity = mapper.toEntity(request);
        VacationRequestEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<VacationRequest> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<VacationRequest> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<VacationRequest> findByEmployeeId(UUID employeeId) {
        return jpaRepository.findByEmployeeId(employeeId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<VacationRequest> findByStatus(VacationStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
