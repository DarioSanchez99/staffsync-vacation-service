package com.staffsync.vacation.domain.port.out;

import com.staffsync.vacation.domain.model.VacationRequest;

public interface VacationEventPort {

    void publishApproved(VacationRequest request);

    void publishRejected(VacationRequest request);
}
