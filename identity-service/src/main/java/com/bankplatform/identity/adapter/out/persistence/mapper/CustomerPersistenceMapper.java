package com.bankplatform.identity.adapter.out.persistence.mapper;

import com.bankplatform.identity.adapter.out.persistence.entity.CustomerJpaEntity;
import com.bankplatform.identity.adapter.out.persistence.entity.KycDocumentJpaEntity;
import com.bankplatform.identity.domain.model.*;
import org.mapstruct.*;

@Mapper(
        componentModel       = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CustomerPersistenceMapper {

    // Domain → JPA
    @Mapping(source = "bvn.value",  target = "bvn")
    @Mapping(source = "kycStatus",  target = "kycStatus",
            qualifiedByName = "statusToString")
    @Mapping(target = "documents",  ignore = true)
    CustomerJpaEntity toJpaEntity(Customer customer);

    @Mapping(source = "documentType", target = "documentType",
            qualifiedByName = "docTypeToString")
    @Mapping(target = "customer", ignore = true)
    KycDocumentJpaEntity toDocumentEntity(KycDocument doc);

    // JPA → Domain (manual because Customer has private constructor)
    default Customer toDomain(CustomerJpaEntity e) {
        if (e == null) return null;
        try {
            Bvn bvn = new Bvn(e.getBvn());
            Customer customer = Customer.create(
                    e.getAuthUserId(), bvn, e.getNin(),
                    e.getFirstName(), e.getLastName(), e.getMiddleName(),
                    e.getDateOfBirth(), e.getPhoneNumber(), e.getEmail()
            );
            if (e.getAddress() != null)
                customer.setAddress(e.getAddress());
            if (e.getStateOfOrigin() != null)
                customer.setStateOfOrigin(e.getStateOfOrigin());

            // Restore KYC status via reflection
            setField(customer, "id", e.getId());
            setField(customer, "kycStatus",
                    KycStatus.valueOf(e.getKycStatus()));
            setField(customer, "kycRejectionReason",
                    e.getKycRejectionReason());
            setField(customer, "createdAt", e.getCreatedAt());
            setField(customer, "updatedAt", e.getUpdatedAt());

            // Restore documents
            if (e.getDocuments() != null) {
                for (KycDocumentJpaEntity d : e.getDocuments()) {
                    customer.getDocuments(); // trigger collection init
                    var field = Customer.class.getDeclaredField("documents");
                    field.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    var list = (java.util.List<KycDocument>) field.get(customer);
                    list.add(new KycDocument(
                            d.getId(),
                            DocumentType.valueOf(d.getDocumentType()),
                            d.getStorageObjectKey(),
                            d.getOriginalFilename(),
                            d.getContentType(),
                            d.getFileSizeBytes(),
                            d.getUploadedAt()
                    ));
                }
            }
            return customer;
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to reconstruct Customer: " + ex.getMessage(), ex
            );
        }
    }

    @Named("statusToString")
    default String statusToString(KycStatus status) {
        return status == null ? null : status.name();
    }

    @Named("docTypeToString")
    default String docTypeToString(DocumentType type) {
        return type == null ? null : type.name();
    }

    private static void setField(Object obj, String name, Object value)
            throws Exception {
        if (value == null) return;
        var field = obj.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(obj, value);
    }
}