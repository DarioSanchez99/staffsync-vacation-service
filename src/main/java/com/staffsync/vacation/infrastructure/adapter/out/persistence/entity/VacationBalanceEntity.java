package com.staffsync.vacation.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "vacation_balances",
        uniqueConstraints = @UniqueConstraint(name = "uk_balance_employee_year", columnNames = {"employee_id", "year"}),
        indexes = @Index(name = "idx_balance_employee_year", columnList = "employee_id, year"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacationBalanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(nullable = false)
    private int year;

    @Column(name = "total_days", nullable = false)
    private int totalDays;

    @Column(name = "used_days", nullable = false)
    private int usedDays;

    @Column(name = "pending_days", nullable = false)
    private int pendingDays;
}
