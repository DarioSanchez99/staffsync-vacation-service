package com.staffsync.vacation.infrastructure.adapter.in.web;

import com.staffsync.vacation.domain.model.VacationRequest;
import com.staffsync.vacation.domain.port.in.VacationUseCase;
import com.staffsync.vacation.infrastructure.adapter.in.web.api.VacationsApi;
import com.staffsync.vacation.infrastructure.adapter.in.web.dto.RejectVacationRequest;
import com.staffsync.vacation.infrastructure.adapter.in.web.dto.ReviewVacationRequest;
import com.staffsync.vacation.infrastructure.adapter.in.web.dto.SubmitVacationRequest;
import com.staffsync.vacation.infrastructure.adapter.in.web.dto.VacationResponse;
import com.staffsync.vacation.infrastructure.adapter.in.web.dto.VacationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class VacationController implements VacationsApi {

    private final VacationUseCase vacationUseCase;

    @Override
    public ResponseEntity<List<VacationResponse>> listVacations() {
        List<VacationResponse> responses = vacationUseCase.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<VacationResponse> submitVacation(SubmitVacationRequest request) {
        VacationRequest domain = VacationRequest.builder()
                .employeeId(request.getEmployeeId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .build();
        VacationRequest created = vacationUseCase.submit(domain);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @Override
    public ResponseEntity<VacationResponse> getVacationById(UUID id) {
        return vacationUseCase.findById(id)
                .map(r -> ResponseEntity.ok(toResponse(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<VacationResponse>> listVacationsByEmployee(UUID employeeId) {
        List<VacationResponse> responses = vacationUseCase.findByEmployee(employeeId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<List<VacationResponse>> listPendingVacations() {
        List<VacationResponse> responses = vacationUseCase.findPending().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<VacationResponse> approveVacation(UUID id, ReviewVacationRequest request) {
        VacationRequest approved = vacationUseCase.approve(id, request.getReviewedBy());
        return ResponseEntity.ok(toResponse(approved));
    }

    @Override
    public ResponseEntity<VacationResponse> rejectVacation(UUID id, RejectVacationRequest request) {
        VacationRequest rejected = vacationUseCase.reject(id, request.getReviewedBy(), request.getReason());
        return ResponseEntity.ok(toResponse(rejected));
    }

    private VacationResponse toResponse(VacationRequest request) {
        VacationResponse response = new VacationResponse();
        response.setId(request.getId());
        response.setEmployeeId(request.getEmployeeId());
        response.setStartDate(request.getStartDate());
        response.setEndDate(request.getEndDate());
        response.setStatus(request.getStatus() != null
                ? VacationStatus.valueOf(request.getStatus().name())
                : null);
        response.setReason(request.getReason());
        response.setReviewedBy(request.getReviewedBy());
        response.setCreatedAt(request.getCreatedAt() != null
                ? request.getCreatedAt().atOffset(java.time.ZoneOffset.UTC)
                : null);
        return response;
    }
}
