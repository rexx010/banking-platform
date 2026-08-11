package com.bankplatform.card.adapter.out.persistence.mapper;

import com.bankplatform.card.adapter.out.persistence.entity.CardJpaEntity;
import com.bankplatform.card.domain.model.*;
import org.mapstruct.*;

import java.time.YearMonth;

@Mapper(
        componentModel       = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CardPersistenceMapper {

    // Domain → JPA
    @Mapping(source = "cardNetwork",  target = "cardNetwork",
            qualifiedByName = "networkToString")
    @Mapping(source = "status",       target = "status",
            qualifiedByName = "statusToString")
    @Mapping(target = "expiryYear",
            expression = "java(card.getExpiryDate().getYear())")
    @Mapping(target = "expiryMonth",
            expression = "java(card.getExpiryDate().getMonthValue())")
    CardJpaEntity toJpaEntity(Card card);

    // JPA → Domain (manual — private constructor)
    default Card toDomain(CardJpaEntity e) {
        if (e == null) return null;
        try {
            var ctor = Card.class.getDeclaredConstructor(
                    String.class, String.class, String.class, String.class,
                    CardNetwork.class, YearMonth.class, CardStatus.class,
                    String.class, long.class, java.time.Instant.class
            );
            ctor.setAccessible(true);
            Card card = ctor.newInstance(
                    e.getId(), e.getCardNumber(), e.getLinkedNuban(),
                    e.getOwnerUserId(),
                    CardNetwork.valueOf(e.getCardNetwork()),
                    YearMonth.of(e.getExpiryYear(), e.getExpiryMonth()),
                    CardStatus.valueOf(e.getStatus()),
                    e.getCardPinHash(), e.getSpendingLimitKobo(),
                    e.getIssuedAt()
            );
            setField(card, "updatedAt", e.getUpdatedAt());
            return card;
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to reconstruct Card: " + ex.getMessage(), ex);
        }
    }

    @Named("networkToString")
    default String networkToString(CardNetwork n) {
        return n == null ? null : n.name();
    }

    @Named("statusToString")
    default String statusToString(CardStatus s) {
        return s == null ? null : s.name();
    }

    private static void setField(Object o, String name, Object val)
            throws Exception {
        if (val == null) return;
        var f = o.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(o, val);
    }
}