package com.staffsync.vacation.domain.port.out;

import com.staffsync.vacation.domain.model.VacationBalance;

import java.util.Optional;
import java.util.UUID;

public interface VacationBalanceRepository {
    Optional<VacationBalance> findByEmployeeAndYear(UUID employeeId, int year);
    VacationBalance save(VacationBalance balance);
}
