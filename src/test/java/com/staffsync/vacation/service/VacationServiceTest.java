package com.staffsync.vacation.service;

import com.staffsync.vacation.domain.model.VacationBalance;
import com.staffsync.vacation.domain.model.VacationRequest;
import com.staffsync.vacation.domain.model.VacationStatus;
import com.staffsync.vacation.domain.port.out.VacationBalanceRepository;
import com.staffsync.vacation.domain.port.out.VacationEventPort;
import com.staffsync.vacation.domain.port.out.VacationRepository;
import com.staffsync.vacation.domain.service.VacationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacationServiceTest {

    @Mock
    private VacationRepository vacationRepository;

    @Mock
    private VacationEventPort vacationEventPort;

    @Mock
    private VacationBalanceRepository vacationBalanceRepository;

    private VacationService vacationService;

    @BeforeEach
    void setUp() {
        vacationService = new VacationService(vacationRepository, vacationEventPort, vacationBalanceRepository);
    }

    @Test
    void submit_createsWithPendingStatus() {
        UUID employeeId = UUID.randomUUID();
        VacationRequest input = VacationRequest.builder()
                .employeeId(employeeId)
                .startDate(LocalDate.of(2024, 1, 15))
                .endDate(LocalDate.of(2024, 1, 20))
                .reason("Annual vacation")
                .build();

        VacationBalance balance = VacationBalance.builder()
                .employeeId(employeeId).year(2024).totalDays(22).usedDays(0).pendingDays(0).build();

        when(vacationRepository.findOverlapping(any(), any(), any())).thenReturn(List.of());
        when(vacationBalanceRepository.findByEmployeeAndYear(any(), anyInt())).thenReturn(Optional.of(balance));
        when(vacationBalanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(vacationRepository.save(any(VacationRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        VacationRequest result = vacationService.submit(input);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(VacationStatus.PENDING);
        assertThat(result.getCreatedAt()).isNotNull();
        verify(vacationRepository).save(any(VacationRequest.class));
        verify(vacationEventPort).publishRequested(any());
    }

    @Test
    void approve_changesStatusAndPublishesEvent() {
        UUID requestId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();

        VacationRequest existing = VacationRequest.builder()
                .id(requestId)
                .employeeId(employeeId)
                .startDate(LocalDate.of(2024, 1, 15))
                .endDate(LocalDate.of(2024, 1, 20))
                .status(VacationStatus.PENDING)
                .build();

        VacationBalance balance = VacationBalance.builder()
                .employeeId(employeeId).year(2024).totalDays(22).usedDays(0).pendingDays(6).build();

        when(vacationRepository.findById(requestId)).thenReturn(Optional.of(existing));
        when(vacationBalanceRepository.findByEmployeeAndYear(any(), anyInt())).thenReturn(Optional.of(balance));
        when(vacationBalanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(vacationRepository.save(any(VacationRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        VacationRequest result = vacationService.approve(requestId, reviewerId, "Juan García");

        assertThat(result.getStatus()).isEqualTo(VacationStatus.APPROVED);
        assertThat(result.getReviewedBy()).isEqualTo(reviewerId);
        assertThat(result.getReviewedByName()).isEqualTo("Juan García");
        verify(vacationRepository).save(any(VacationRequest.class));
        verify(vacationEventPort).publishApproved(any(VacationRequest.class));
        verify(vacationEventPort, never()).publishRejected(any());
    }

    @Test
    void reject_changesStatusAndPublishesEvent() {
        UUID requestId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        String rejectionReason = "Insufficient staff coverage";

        VacationRequest existing = VacationRequest.builder()
                .id(requestId)
                .employeeId(employeeId)
                .startDate(LocalDate.of(2024, 1, 15))
                .endDate(LocalDate.of(2024, 1, 20))
                .status(VacationStatus.PENDING)
                .build();

        VacationBalance balance = VacationBalance.builder()
                .employeeId(employeeId).year(2024).totalDays(22).usedDays(0).pendingDays(6).build();

        when(vacationRepository.findById(requestId)).thenReturn(Optional.of(existing));
        when(vacationBalanceRepository.findByEmployeeAndYear(any(), anyInt())).thenReturn(Optional.of(balance));
        when(vacationBalanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(vacationRepository.save(any(VacationRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        VacationRequest result = vacationService.reject(requestId, reviewerId, "Ana López", rejectionReason);

        assertThat(result.getStatus()).isEqualTo(VacationStatus.REJECTED);
        assertThat(result.getReviewedBy()).isEqualTo(reviewerId);
        assertThat(result.getReviewedByName()).isEqualTo("Ana López");
        assertThat(result.getRejectionReason()).isEqualTo(rejectionReason);
        verify(vacationRepository).save(any(VacationRequest.class));
        verify(vacationEventPort).publishRejected(any(VacationRequest.class));
        verify(vacationEventPort, never()).publishApproved(any());
    }
}
