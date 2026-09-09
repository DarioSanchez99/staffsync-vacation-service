package com.staffsync.vacation.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacationRequest {

    private UUID id;
    private UUID employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private VacationStatus status;
    private String reason;
    private UUID reviewedBy;
    private LocalDateTime createdAt;
}
