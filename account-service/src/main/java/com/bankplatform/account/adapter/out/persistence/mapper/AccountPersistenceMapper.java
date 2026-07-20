package com.bankplatform.account.adapter.out.persistence.mapper;

import com.bankplatform.account.adapter.out.persistence.entity.AccountJpaEntity;
import com.bankplatform.account.domain.model.*;
import com.bankplatform.shared.domain.Money;
import org.mapstruct.*;

@Mapper(
        componentModel       = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AccountPersistenceMapper {

    // Domain → JPA
    @Mapping(source = "accountNumber.value", target = "accountNumber")
    @Mapping(source = "bankCode.value",      target = "bankCode")
    @Mapping(source = "accountType",         target = "accountType",
            qualifiedByName = "typeToString")
    @Mapping(source = "status",              target = "status",
            qualifiedByName = "statusToString")
    @Mapping(target = "balanceKobo",
            expression = "java(account.getBalance().getAmountInMinorUnits())")
    AccountJpaEntity toJpaEntity(Account account);

    // JPA → Domain (manual reconstruction)
    default Account toDomain(AccountJpaEntity e) {
        if (e == null) return null;
        try {
            var constructor = Account.class.getDeclaredConstructor(
                    String.class,        // id
                    AccountNumber.class, // accountNumber
                    BankCode.class,      // bankCode
                    String.class,        // ownerBvn
                    AccountType.class,   // accountType
                    String.class,        // currency
                    Money.class,         // balance
                    AccountStatus.class, // status
                    long.class,          // version
                    java.time.Instant.class // createdAt
            );
            constructor.setAccessible(true);

            return constructor.newInstance(
                    e.getId(),
                    new AccountNumber(e.getAccountNumber()),
                    new BankCode(e.getBankCode()),
                    e.getOwnerBvn(),
                    AccountType.valueOf(e.getAccountType()),
                    e.getCurrency(),
                    Money.of(e.getBalanceKobo(), e.getCurrency()),
                    AccountStatus.valueOf(e.getStatus()),
                    e.getVersion(),
                    e.getCreatedAt()
            );
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to reconstruct Account: " + ex.getMessage(), ex
            );
        }
    }

    @Named("typeToString")
    default String typeToString(AccountType type) {
        return type == null ? null : type.name();
    }

    @Named("statusToString")
    default String statusToString(AccountStatus status) {
        return status == null ? null : status.name();
    }
}