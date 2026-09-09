package com.staffsync.vacation.domain.service;

import com.staffsync.vacation.domain.model.VacationRequest;
import com.staffsync.vacation.domain.model.VacationStatus;
import com.staffsync.vacation.domain.port.in.VacationUseCase;
import com.staffsync.vacation.domain.port.out.VacationEventPort;
import com.staffsync.vacation.domain.port.out.VacationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VacationService implements VacationUseCase {

    private final VacationRepository vacationRepository;
    private final VacationEventPort vacationEventPort;

    @Override
    public VacationRequest submit(VacationRequest request) {
        request.setId(UUID.randomUUID());
        request.setStatus(VacationStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        return vacationRepository.save(request);
    }

    @Override
    public VacationRequest approve(UUID id, UUID reviewedBy) {
        VacationRequest existing = vacationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Vacation request not found: " + id));

        existing.setStatus(VacationStatus.APPROVED);
        existing.setReviewedBy(reviewedBy);

        VacationRequest saved = vacationRepository.save(existing);
        vacationEventPort.publishApproved(saved);
        return saved;
    }

    @Override
    public VacationRequest reject(UUID id, UUID reviewedBy, String reason) {
        VacationRequest existing = vacationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Vacation request not found: " + id));

        existing.setStatus(VacationStatus.REJECTED);
        existing.setReviewedBy(reviewedBy);
        if (reason != null) {
            existing.setReason(reason);
        }

        VacationRequest saved = vacationRepository.save(existing);
        vacationEventPort.publishRejected(saved);
        return saved;
    }

    @Override
    public Optional<VacationRequest> findById(UUID id) {
        return vacationRepository.findById(id);
    }

    @Override
    public List<VacationRequest> findByEmployee(UUID employeeId) {
        return vacationRepository.findByEmployeeId(employeeId);
    }

    @Override
    public List<VacationRequest> findPending() {
        return vacationRepository.findByStatus(VacationStatus.PENDING);
    }

    @Override
    public List<VacationRequest> findAll() {
        return vacationRepository.findAll();
    }
}
