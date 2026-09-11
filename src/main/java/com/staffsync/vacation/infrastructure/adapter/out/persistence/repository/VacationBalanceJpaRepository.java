package com.staffsync.vacation.infrastructure.adapter.out.persistence.repository;

import com.staffsync.vacation.infrastructure.adapter.out.persistence.entity.VacationBalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VacationBalanceJpaRepository extends JpaRepository<VacationBalanceEntity, UUID> {
    Optional<VacationBalanceEntity> findByEmployeeIdAndYear(UUID employeeId, int year);
}
