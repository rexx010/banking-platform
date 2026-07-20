package com.bankplatform.identity.domain.port.out;

import com.bankplatform.identity.domain.model.Customer;
import java.util.Optional;

/**
 * OUT-PORT: what the domain needs from persistence.
 * Zero JPA imports — domain has no knowledge of the database.
 */
public interface CustomerRepository {
    Customer           save(Customer customer);
    Optional<Customer> findById(String id);
    Optional<Customer> findByBvn(String bvn);
    Optional<Customer> findByAuthUserId(String authUserId);
    Optional<Customer> findByNin(String nin);
    boolean            existsByBvn(String bvn);
    boolean            existsByNin(String nin);
}