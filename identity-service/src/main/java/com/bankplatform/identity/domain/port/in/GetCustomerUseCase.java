package com.bankplatform.identity.domain.port.in;

import com.bankplatform.identity.domain.model.Customer;
import java.util.Optional;

public interface GetCustomerUseCase {
    Customer        getByBvn(String bvn);
    Customer        getByAuthUserId(String authUserId);
    Optional<Customer> findByBvn(String bvn);
}