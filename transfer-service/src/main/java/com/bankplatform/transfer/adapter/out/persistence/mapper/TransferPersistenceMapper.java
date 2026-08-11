package com.bankplatform.transfer.adapter.out.persistence.mapper;

import com.bankplatform.transfer.adapter.out.persistence.entity.TransferJpaEntity;
import com.bankplatform.transfer.domain.model.Transfer;
import com.bankplatform.transfer.domain.model.TransferStatus;
import org.mapstruct.*;

@Mapper(
        componentModel       = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TransferPersistenceMapper {

    // Domain → JPA
    @Mapping(source = "status", target = "status",
            qualifiedByName = "statusToString")
    TransferJpaEntity toJpaEntity(Transfer transfer);

    // JPA → Domain (manual — private constructor)
    default Transfer toDomain(TransferJpaEntity e) {
        if (e == null) return null;
        try {
            var ctor = Transfer.class.getDeclaredConstructor(
                    String.class, String.class, String.class, String.class,
                    String.class, long.class, String.class, String.class,
                    String.class, TransferStatus.class, String.class,
                    java.time.Instant.class
            );
            ctor.setAccessible(true);
            Transfer t = ctor.newInstance(
                    e.getId(), e.getIdempotencyKey(),
                    e.getSourceAccountNumber(),
                    e.getDestinationAccountNumber(),
                    e.getDestinationBankCode(),
                    e.getAmountKobo(), e.getCurrency(),
                    e.getNarration(), e.getInitiatedByUserId(),
                    TransferStatus.valueOf(e.getStatus()),
                    e.getFailureReason(), e.getCreatedAt()
            );
            if (e.getCompletedAt() != null) {
                setField(t, "completedAt", e.getCompletedAt());
            }
            setField(t, "updatedAt", e.getUpdatedAt());
            return t;
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to reconstruct Transfer: " + ex.getMessage(), ex);
        }
    }

    @Named("statusToString")
    default String statusToString(TransferStatus s) {
        return s == null ? null : s.name();
    }

    private static void setField(Object obj, String name, Object val)
            throws Exception {
        if (val == null) return;
        var f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, val);
    }
}