package com.bankplatform.transfer.domain.port.in;

import com.bankplatform.transfer.domain.model.Transfer;
import java.util.List;

public interface GetTransferUseCase {
    Transfer getById(String transferId);
    List<Transfer> getByAccountNumber(String accountNumber);
}