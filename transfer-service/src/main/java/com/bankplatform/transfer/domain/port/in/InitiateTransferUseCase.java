package com.bankplatform.transfer.domain.port.in;

import com.bankplatform.transfer.application.usecase.TransferCommands.InitiateTransferCommand;
import com.bankplatform.transfer.domain.model.Transfer;

/**
 * IN-PORT: initiates a money transfer.
 *
 * Runs the full SAGA:
 *   1. Idempotency check
 *   2. Validate accounts and balance
 *   3. Debit source account
 *   4. Credit destination account
 *   5. If credit fails, reverse the debit
 *   6. Publish completion or failure event
 */
public interface InitiateTransferUseCase {
    Transfer initiate(InitiateTransferCommand command);
}