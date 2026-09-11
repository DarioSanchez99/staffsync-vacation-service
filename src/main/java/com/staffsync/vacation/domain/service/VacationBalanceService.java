package com.staffsync.vacation.domain.service;

import com.staffsync.vacation.domain.model.VacationBalance;
import com.staffsync.vacation.domain.port.in.VacationBalanceUseCase;
import com.staffsync.vacation.domain.port.out.VacationBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VacationBalanceService implements VacationBalanceUseCase {

    private final VacationBalanceRepository vacationBalanceRepository;

    @Override
    public VacationBalance getBalance(UUID employeeId, int year) {
        return vacationBalanceRepository.findByEmployeeAndYear(employeeId, year)
                .orElse(VacationBalance.builder()
                        .employeeId(employeeId)
                        .year(year)
                        .totalDays(22)
                        .usedDays(0)
                        .pendingDays(0)
                        .build());
    }
}
