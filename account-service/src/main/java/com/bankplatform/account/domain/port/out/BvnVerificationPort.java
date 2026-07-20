package com.bankplatform.account.domain.port.out;

public interface BvnVerificationPort {
    boolean isBvnVerified(String bvn);
    String getCustomerName(String bvn);
}