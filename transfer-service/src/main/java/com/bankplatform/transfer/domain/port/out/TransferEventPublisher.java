package com.bankplatform.transfer.domain.port.out;

import com.bankplatform.transfer.domain.model.Transfer;

public interface TransferEventPublisher {
    void publishInitiated(Transfer transfer);
    void publishCompleted(Transfer transfer);
    void publishFailed(Transfer transfer);
    void publishReversed(Transfer transfer);
}