package com.staffsync.vacation.infrastructure.adapter.out.persistence.adapter;

import com.staffsync.vacation.domain.model.VacationBalance;
import com.staffsync.vacation.domain.port.out.VacationBalanceRepository;
import com.staffsync.vacation.infrastructure.adapter.out.persistence.entity.VacationBalanceEntity;
import com.staffsync.vacation.infrastructure.adapter.out.persistence.repository.VacationBalanceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VacationBalanceRepositoryAdapter implements VacationBalanceRepository {

    private final VacationBalanceJpaRepository jpaRepository;

    @Override
    public Optional<VacationBalance> findByEmployeeAndYear(UUID employeeId, int year) {
        return jpaRepository.findByEmployeeIdAndYear(employeeId, year).map(this::toDomain);
    }

    @Override
    public VacationBalance save(VacationBalance balance) {
        VacationBalanceEntity entity = toEntity(balance);
        return toDomain(jpaRepository.save(entity));
    }

    private VacationBalanceEntity toEntity(VacationBalance b) {
        return VacationBalanceEntity.builder()
                .id(b.getId())
                .employeeId(b.getEmployeeId())
                .year(b.getYear())
                .totalDays(b.getTotalDays())
                .usedDays(b.getUsedDays())
                .pendingDays(b.getPendingDays())
                .build();
    }

    private VacationBalance toDomain(VacationBalanceEntity e) {
        return VacationBalance.builder()
                .id(e.getId())
                .employeeId(e.getEmployeeId())
                .year(e.getYear())
                .totalDays(e.getTotalDays())
                .usedDays(e.getUsedDays())
                .pendingDays(e.getPendingDays())
                .build();
    }
}
