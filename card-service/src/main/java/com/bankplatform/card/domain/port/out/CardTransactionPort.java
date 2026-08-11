package com.bankplatform.card.domain.port.out;

/**
 * OUT-PORT: debits the account linked to a card.
 *
 * Implemented by TransferServiceClient which calls
 * transfer-service's account debit endpoint.
 *
 * Card transactions are essentially debits on the linked account.
 * The card authorises the debit — transfer-service executes it.
 */
public interface CardTransactionPort {
    void debitAccount(
            String accountNumber,
            long   amountKobo,
            String currency,
            String idempotencyKey
    );
}