package com.staffsync.vacation.service;

import com.staffsync.vacation.domain.model.VacationRequest;
import com.staffsync.vacation.domain.model.VacationStatus;
import com.staffsync.vacation.domain.port.out.VacationEventPort;
import com.staffsync.vacation.domain.port.out.VacationRepository;
import com.staffsync.vacation.domain.service.VacationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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

    private VacationService vacationService;

    @BeforeEach
    void setUp() {
        vacationService = new VacationService(vacationRepository, vacationEventPort);
    }

    @Test
    void submit_createsWithPendingStatus() {
        // Given
        VacationRequest input = VacationRequest.builder()
                .employeeId(UUID.randomUUID())
                .startDate(LocalDate.of(2024, 1, 15))
                .endDate(LocalDate.of(2024, 1, 20))
                .reason("Annual vacation")
                .build();

        when(vacationRepository.save(any(VacationRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // When
        VacationRequest result = vacationService.submit(input);

        // Then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(VacationStatus.PENDING);
        assertThat(result.getCreatedAt()).isNotNull();

        verify(vacationRepository).save(any(VacationRequest.class));
        verifyNoInteractions(vacationEventPort);
    }

    @Test
    void approve_changesStatusAndPublishesEvent() {
        // Given
        UUID requestId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();

        VacationRequest existing = VacationRequest.builder()
                .id(requestId)
                .employeeId(UUID.randomUUID())
                .startDate(LocalDate.of(2024, 1, 15))
                .endDate(LocalDate.of(2024, 1, 20))
                .status(VacationStatus.PENDING)
                .build();

        when(vacationRepository.findById(requestId)).thenReturn(Optional.of(existing));
        when(vacationRepository.save(any(VacationRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // When
        VacationRequest result = vacationService.approve(requestId, reviewerId);

        // Then
        assertThat(result.getStatus()).isEqualTo(VacationStatus.APPROVED);
        assertThat(result.getReviewedBy()).isEqualTo(reviewerId);

        verify(vacationRepository).save(any(VacationRequest.class));
        verify(vacationEventPort).publishApproved(any(VacationRequest.class));
        verify(vacationEventPort, never()).publishRejected(any());
    }

    @Test
    void reject_changesStatusAndPublishesEvent() {
        // Given
        UUID requestId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        String rejectionReason = "Insufficient staff coverage";

        VacationRequest existing = VacationRequest.builder()
                .id(requestId)
                .employeeId(UUID.randomUUID())
                .startDate(LocalDate.of(2024, 1, 15))
                .endDate(LocalDate.of(2024, 1, 20))
                .status(VacationStatus.PENDING)
                .build();

        when(vacationRepository.findById(requestId)).thenReturn(Optional.of(existing));
        when(vacationRepository.save(any(VacationRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // When
        VacationRequest result = vacationService.reject(requestId, reviewerId, rejectionReason);

        // Then
        assertThat(result.getStatus()).isEqualTo(VacationStatus.REJECTED);
        assertThat(result.getReviewedBy()).isEqualTo(reviewerId);
        assertThat(result.getReason()).isEqualTo(rejectionReason);

        verify(vacationRepository).save(any(VacationRequest.class));
        verify(vacationEventPort).publishRejected(any(VacationRequest.class));
        verify(vacationEventPort, never()).publishApproved(any());
    }
}
