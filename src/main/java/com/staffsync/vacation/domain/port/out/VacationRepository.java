package com.staffsync.vacation.domain.port.out;

import com.staffsync.vacation.domain.model.VacationRequest;
import com.staffsync.vacation.domain.model.VacationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VacationRepository {

    VacationRequest save(VacationRequest request);

    Optional<VacationRequest> findById(UUID id);

    List<VacationRequest> findAll();

    List<VacationRequest> findByEmployeeId(UUID employeeId);

    List<VacationRequest> findByStatus(VacationStatus status);

    List<VacationRequest> findOverlapping(UUID employeeId, LocalDate startDate, LocalDate endDate);
}
