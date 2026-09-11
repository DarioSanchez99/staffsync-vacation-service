package com.staffsync.vacation.infrastructure.adapter.out.persistence.repository;

import com.staffsync.vacation.domain.model.VacationStatus;
import com.staffsync.vacation.infrastructure.adapter.out.persistence.entity.VacationRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface VacationJpaRepository extends JpaRepository<VacationRequestEntity, UUID> {

    List<VacationRequestEntity> findByEmployeeId(UUID employeeId);

    List<VacationRequestEntity> findByStatus(VacationStatus status);

    @Query("SELECT v FROM VacationRequestEntity v WHERE v.employeeId = :employeeId " +
           "AND v.status IN ('PENDING', 'APPROVED') " +
           "AND v.startDate <= :endDate AND v.endDate >= :startDate")
    List<VacationRequestEntity> findOverlapping(
            @Param("employeeId") UUID employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
