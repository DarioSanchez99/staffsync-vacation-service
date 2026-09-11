package com.staffsync.vacation.domain.port.in;

import com.staffsync.vacation.domain.model.VacationRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VacationUseCase {

    VacationRequest submit(VacationRequest request);

    VacationRequest approve(UUID id, UUID reviewedBy, String reviewedByName);

    VacationRequest reject(UUID id, UUID reviewedBy, String reviewedByName, String reason);

    Optional<VacationRequest> findById(UUID id);

    List<VacationRequest> findByEmployee(UUID employeeId);

    List<VacationRequest> findPending();

    List<VacationRequest> findAll();
}
