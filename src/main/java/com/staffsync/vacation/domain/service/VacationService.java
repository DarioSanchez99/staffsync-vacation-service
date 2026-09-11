package com.staffsync.vacation.domain.service;

import com.staffsync.vacation.domain.model.VacationBalance;
import com.staffsync.vacation.domain.model.VacationRequest;
import com.staffsync.vacation.domain.model.VacationStatus;
import com.staffsync.vacation.domain.port.in.VacationUseCase;
import com.staffsync.vacation.domain.port.out.VacationBalanceRepository;
import com.staffsync.vacation.domain.port.out.VacationEventPort;
import com.staffsync.vacation.domain.port.out.VacationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VacationService implements VacationUseCase {

    private final VacationRepository vacationRepository;
    private final VacationEventPort vacationEventPort;
    private final VacationBalanceRepository vacationBalanceRepository;

    @Override
    public VacationRequest submit(VacationRequest request) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        List<VacationRequest> overlapping = vacationRepository.findOverlapping(
                request.getEmployeeId(), request.getStartDate(), request.getEndDate());
        if (!overlapping.isEmpty()) {
            throw new IllegalStateException("Employee already has a vacation request overlapping these dates");
        }

        int days = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        int year = request.getStartDate().getYear();
        VacationBalance balance = getOrCreateBalance(request.getEmployeeId(), year);

        if (balance.availableDays() < days) {
            throw new IllegalStateException(
                    "Insufficient vacation days: requested " + days + " but only " + balance.availableDays() + " available");
        }

        balance.setPendingDays(balance.getPendingDays() + days);
        vacationBalanceRepository.save(balance);

        request.setId(UUID.randomUUID());
        request.setStatus(VacationStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        VacationRequest saved = vacationRepository.save(request);
        vacationEventPort.publishRequested(saved);
        return saved;
    }

    @Override
    public VacationRequest approve(UUID id, UUID reviewedBy, String reviewedByName) {
        VacationRequest existing = vacationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Vacation request not found: " + id));

        int days = (int) ChronoUnit.DAYS.between(existing.getStartDate(), existing.getEndDate()) + 1;
        int year = existing.getStartDate().getYear();
        VacationBalance balance = getOrCreateBalance(existing.getEmployeeId(), year);
        balance.setUsedDays(balance.getUsedDays() + days);
        balance.setPendingDays(Math.max(0, balance.getPendingDays() - days));
        vacationBalanceRepository.save(balance);

        existing.setStatus(VacationStatus.APPROVED);
        existing.setReviewedBy(reviewedBy);
        existing.setReviewedByName(reviewedByName);

        VacationRequest saved = vacationRepository.save(existing);
        vacationEventPort.publishApproved(saved);
        return saved;
    }

    @Override
    public VacationRequest reject(UUID id, UUID reviewedBy, String reviewedByName, String reason) {
        VacationRequest existing = vacationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Vacation request not found: " + id));

        int days = (int) ChronoUnit.DAYS.between(existing.getStartDate(), existing.getEndDate()) + 1;
        int year = existing.getStartDate().getYear();
        VacationBalance balance = getOrCreateBalance(existing.getEmployeeId(), year);
        balance.setPendingDays(Math.max(0, balance.getPendingDays() - days));
        vacationBalanceRepository.save(balance);

        existing.setStatus(VacationStatus.REJECTED);
        existing.setReviewedBy(reviewedBy);
        existing.setReviewedByName(reviewedByName);
        if (reason != null) {
            existing.setRejectionReason(reason);
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

    private VacationBalance getOrCreateBalance(UUID employeeId, int year) {
        return vacationBalanceRepository.findByEmployeeAndYear(employeeId, year)
                .orElse(VacationBalance.builder()
                        .employeeId(employeeId)
                        .year(year)
                        .totalDays(22)
                        .usedDays(0)
                        .pendingDays(0)
                        .build());
    }
}
