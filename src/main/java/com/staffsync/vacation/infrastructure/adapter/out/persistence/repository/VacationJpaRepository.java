package com.staffsync.vacation.infrastructure.adapter.out.persistence.repository;

import com.staffsync.vacation.domain.model.VacationStatus;
import com.staffsync.vacation.infrastructure.adapter.out.persistence.entity.VacationRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VacationJpaRepository extends JpaRepository<VacationRequestEntity, UUID> {

    List<VacationRequestEntity> findByEmployeeId(UUID employeeId);

    List<VacationRequestEntity> findByStatus(VacationStatus status);
}
