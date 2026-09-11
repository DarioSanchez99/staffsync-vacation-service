package com.staffsync.vacation.infrastructure.adapter.out.persistence.entity;

import com.staffsync.vacation.domain.model.VacationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vacation_requests", indexes = {
        @jakarta.persistence.Index(name = "idx_vacation_employee", columnList = "employee_id"),
        @jakarta.persistence.Index(name = "idx_vacation_status", columnList = "status"),
        @jakarta.persistence.Index(name = "idx_vacation_employee_status", columnList = "employee_id, status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacationRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VacationStatus status;

    @Column(length = 1000)
    private String reason;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_by_name", length = 255)
    private String reviewedByName;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
