package com.staffsync.vacation.infrastructure.adapter.in.web;

import com.staffsync.vacation.domain.model.VacationBalance;
import com.staffsync.vacation.domain.model.VacationRequest;
import com.staffsync.vacation.domain.port.in.VacationBalanceUseCase;
import com.staffsync.vacation.domain.port.in.VacationUseCase;
import com.staffsync.vacation.infrastructure.adapter.in.web.api.VacationsApi;
import com.staffsync.vacation.infrastructure.adapter.in.web.dto.RejectVacationRequest;
import com.staffsync.vacation.infrastructure.adapter.in.web.dto.ReviewVacationRequest;
import com.staffsync.vacation.infrastructure.adapter.in.web.dto.SubmitVacationRequest;
import com.staffsync.vacation.infrastructure.adapter.in.web.dto.VacationResponse;
import com.staffsync.vacation.infrastructure.adapter.in.web.dto.VacationStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class VacationController implements VacationsApi {

    private final VacationUseCase vacationUseCase;
    private final VacationBalanceUseCase vacationBalanceUseCase;
    private final HttpServletRequest httpServletRequest;

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
        String reviewedByName = httpServletRequest.getHeader("X-User-Name");
        VacationRequest approved = vacationUseCase.approve(id, request.getReviewedBy(), reviewedByName);
        return ResponseEntity.ok(toResponse(approved));
    }

    @Override
    public ResponseEntity<VacationResponse> rejectVacation(UUID id, RejectVacationRequest request) {
        String reviewedByName = httpServletRequest.getHeader("X-User-Name");
        VacationRequest rejected = vacationUseCase.reject(id, request.getReviewedBy(), reviewedByName, request.getReason());
        return ResponseEntity.ok(toResponse(rejected));
    }

    @GetMapping("/vacations/balance/{employeeId}")
    public ResponseEntity<Map<String, Object>> getBalance(
            @PathVariable UUID employeeId,
            @RequestParam(defaultValue = "0") int year) {
        int resolvedYear = year > 0 ? year : LocalDate.now().getYear();
        VacationBalance balance = vacationBalanceUseCase.getBalance(employeeId, resolvedYear);
        Map<String, Object> result = new HashMap<>();
        result.put("employeeId", balance.getEmployeeId());
        result.put("year", balance.getYear());
        result.put("totalDays", balance.getTotalDays());
        result.put("usedDays", balance.getUsedDays());
        result.put("pendingDays", balance.getPendingDays());
        result.put("availableDays", balance.availableDays());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/vacations/paged")
    public ResponseEntity<Map<String, Object>> listVacationsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<VacationRequest> all = vacationUseCase.findAll();
        return ResponseEntity.ok(buildPage(all, page, size));
    }

    @GetMapping("/vacations/pending/paged")
    public ResponseEntity<Map<String, Object>> listPendingVacationsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<VacationRequest> pending = vacationUseCase.findPending();
        return ResponseEntity.ok(buildPage(pending, page, size));
    }

    private Map<String, Object> buildPage(List<VacationRequest> all, int page, int size) {
        int total = all.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<VacationResponse> content = all.subList(fromIndex, toIndex).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("content", content);
        result.put("totalElements", (long) total);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        result.put("page", page);
        result.put("size", size);
        return result;
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
        response.setReviewedByName(request.getReviewedByName());
        response.setRejectionReason(request.getRejectionReason());
        response.setCreatedAt(request.getCreatedAt() != null
                ? request.getCreatedAt().atOffset(java.time.ZoneOffset.UTC)
                : null);
        return response;
    }
}
