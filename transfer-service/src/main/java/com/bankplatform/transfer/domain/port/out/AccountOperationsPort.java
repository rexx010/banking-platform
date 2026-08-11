package com.bankplatform.transfer.domain.port.out;

/**
 * OUT-PORT: operations on accounts managed by account-service.
 *
 * Implemented by AccountServiceClient which calls account-service
 * via its internal REST API.
 *
 * The domain does not know an HTTP call is happening.
 * It just calls debit() and credit() and gets a result.
 */
public interface AccountOperationsPort {

    /**
     * Debits the source account.
     * Throws BankException if account not found,
     * insufficient funds, or account not active.
     */
    void debit(String accountNumber, long amountKobo,
               String currency, String reference);

    /**
     * Credits the destination account.
     * Throws BankException if account not found
     * or account is suspended/closed.
     */
    void credit(String accountNumber, long amountKobo,
                String currency, String reference);

    /**
     * Verifies an account exists and is active.
     * Called before initiating a transfer to fail fast
     * if the destination account is invalid.
     */
    boolean isAccountActive(String accountNumber);
}