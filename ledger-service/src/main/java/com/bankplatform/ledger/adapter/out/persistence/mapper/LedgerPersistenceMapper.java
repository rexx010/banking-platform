package com.bankplatform.ledger.adapter.out.persistence.mapper;

import com.bankplatform.ledger.adapter.out.persistence.entity.LedgerEntryJpaEntity;
import com.bankplatform.ledger.domain.model.EntryType;
import com.bankplatform.ledger.domain.model.LedgerEntry;
import com.bankplatform.shared.domain.Money;
import org.mapstruct.*;

@Mapper(
        componentModel       = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface LedgerPersistenceMapper {

    // Domain → JPA
    @Mapping(source = "entryType",  target = "entryType",
            qualifiedByName = "typeToString")
    @Mapping(target = "amountKobo",
            expression = "java(entry.amount().getAmountInMinorUnits())")
    @Mapping(target = "currency",
            expression = "java(entry.amount().getCurrency().getCurrencyCode())")
    LedgerEntryJpaEntity toJpaEntity(LedgerEntry entry);

    // JPA → Domain
    // LedgerEntry is a record — MapStruct uses its canonical constructor
    @Mapping(target = "amount",
            expression = """
                 java(com.bankplatform.shared.domain.Money.of(
                     entity.getAmountKobo(), entity.getCurrency()))
                 """)
    @Mapping(target = "entryType",
            expression = "java(com.bankplatform.ledger.domain.model" +
                    ".EntryType.valueOf(entity.getEntryType()))")
    LedgerEntry toDomain(LedgerEntryJpaEntity entity);

    @Named("typeToString")
    default String typeToString(EntryType type) {
        return type == null ? null : type.name();
    }
}