package com.bankplatform.transfer.domain.port.out;

import com.bankplatform.transfer.domain.model.Transfer;
import java.util.List;
import java.util.Optional;

public interface TransferRepository {
    Transfer           save(Transfer transfer);
    Optional<Transfer> findById(String id);
    Optional<Transfer> findByIdempotencyKey(String key);
    List<Transfer>     findBySourceAccountNumber(String accountNumber);
}