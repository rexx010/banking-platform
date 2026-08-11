package com.bankplatform.ledger.domain.port.in;

import com.bankplatform.ledger.application.usecase.LedgerCommands.RecordTransactionCommand;
import com.bankplatform.ledger.domain.model.LedgerTransaction;

/**
 * IN-PORT: records a completed money movement in the ledger.
 *
 * Called by the Kafka consumer when a TransferCompletedEvent
 * or CardTransactionEvent arrives.
 *
 * Creates two ledger entries atomically:
 *   - DEBIT entry on the source account
 *   - CREDIT entry on the destination account
 *
 * The ledger is append-only — no updates, no deletes.
 */
public interface RecordTransactionUseCase {
    LedgerTransaction record(RecordTransactionCommand command);
}