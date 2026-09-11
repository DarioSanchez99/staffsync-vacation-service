package com.staffsync.vacation.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacationBalance {

    private UUID id;
    private UUID employeeId;
    private int year;
    private int totalDays;
    private int usedDays;
    private int pendingDays;

    public int availableDays() {
        return totalDays - usedDays - pendingDays;
    }
}
