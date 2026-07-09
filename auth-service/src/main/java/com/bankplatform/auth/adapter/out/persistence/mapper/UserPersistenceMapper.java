package com.bankplatform.auth.adapter.out.persistence.mapper;

import com.bankplatform.auth.adapter.out.persistence.entity.UserJpaEntity;
import com.bankplatform.auth.domain.model.Role;
import com.bankplatform.auth.domain.model.User;
import com.bankplatform.auth.domain.model.UserStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Set;
import java.util.stream.Collectors;


/**
 * Maps between domain User and UserJpaEntity.
 *
 * Domain → JPA: called when saving a user to the database.
 * JPA → Domain: called when loading a user from the database.
 *
 * The domain User has a private constructor so we cannot
 * use MapStruct's standard object creation. We handle the
 * JPA → Domain direction manually using a default method.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserPersistenceMapper {
//    Domain -> JPA
    @Mapping(
            source = "status",
            target = "status",
            qualifiedByName = "statusToString"
    )
    @Mapping(
            source = "roles",
            target = "roles",
            qualifiedByName = "rolesToString"
    )
    UserJpaEntity toJpaEntity(User user);

//    JPA -> Domain
    /**
     * Manual reconstruction because User has a private constructor.
     * We call User.register() to get a valid instance then use
     * reflection to restore the remaining fields.
     * This is acceptable in the persistence adapter — infrastructure
     * glue code. The domain itself stays clean.
     */
    default User toDomain(UserJpaEntity entity){
        if (entity == null) {
            return null;
        }
        try{
//            Use the private constructor via reflection
            var constructor = User.class.getDeclaredConstructor(
                    String.class, // id
                    String.class, // email
                    String.class, // passwordHas
                    String.class, // phone number
                    java.time.Instant.class
                    );
            constructor.setAccessible(true);
            User user = constructor.newInstance(
                    entity.getId(),
                    entity.getEmail(),
                    entity.getPasswordHash(),
                    entity.getPhoneNumber(),
                    entity.getCreatedAt()
            );

//            Restore remaining private fields
            setField(user, "transactionPinHash",
                    entity.getTransactionPinHash());
            setField(user, "status",
                    UserStatus.valueOf(entity.getStatus()));
            setField(user, "failedLoginAttempts",
                    entity.getFailedLoginAttempts());
            setField(user, "lockedUntil",
                    entity.getLockedUntil());
            setField(user, "lastLoginAt",
                    entity.getLastLoginAt());
            setField(user, "updatedAt",
                    entity.getUpdatedAt());

//            Restore roles
            var rolesField = User.class.getDeclaredField("roles");
            rolesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            var rolesSet = (java.util.Set<Role>) rolesField.get(user);
            entity.getRoles().stream()
                    .map(Role::valueOf)
                    .forEach(rolesSet::add);

            return user;
        }catch(Exception e){
            throw new IllegalStateException(
                    "Failed to reconstruct User from database: "
                    + e.getMessage(), e
            );
        }
    }

//    Converters
    @Named("statusToString")
    default String statusToString(UserStatus status){
        return status == null ? null : status.name();
    }

    @Named("rolesToString")
    default Set<String> rolesToStrings(Set<Role> roles){
        if(roles == null) return Set.of();
        return roles.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    private static void setField(
            Object obj, String filedName, Object value
    ) throws Exception{
        if (value == null) return;
        var field = obj.getClass().getDeclaredField(filedName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}
