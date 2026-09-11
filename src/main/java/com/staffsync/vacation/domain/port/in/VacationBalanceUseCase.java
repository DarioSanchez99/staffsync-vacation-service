package com.staffsync.vacation.domain.port.in;

import com.staffsync.vacation.domain.model.VacationBalance;

import java.util.UUID;

public interface VacationBalanceUseCase {
    VacationBalance getBalance(UUID employeeId, int year);
}
