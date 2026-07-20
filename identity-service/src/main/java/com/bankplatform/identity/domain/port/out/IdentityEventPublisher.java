package com.bankplatform.identity.domain.port.out;

import com.bankplatform.identity.domain.model.Customer;

public interface IdentityEventPublisher {
    void publishBvnCreated(Customer customer);
    void publishKycVerified(Customer customer);
    void publishKycRejected(Customer customer);
}