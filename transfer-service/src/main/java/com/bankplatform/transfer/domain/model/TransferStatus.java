package com.bankplatform.transfer.domain.model;

/**
 * Tracks the lifecycle of a transfer through the SAGA.
 *
 * PENDING:     Request received, validation in progress
 * PROCESSING:  Source account debited, credit in progress
 * COMPLETED:   Both debit and credit succeeded
 * FAILED:      Transfer could not be completed (pre-debit)
 * REVERSED:    Debit was made but credit failed — debit reversed
 *
 * Terminal states: COMPLETED, FAILED, REVERSED
 * A transfer in a terminal state cannot change status.
 */
public enum TransferStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REVERSED
}