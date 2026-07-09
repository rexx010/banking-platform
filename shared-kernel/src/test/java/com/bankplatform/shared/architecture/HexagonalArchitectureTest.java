package com.bankplatform.shared.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class HexagonalArchitectureTest {
    private JavaClasses classes;
    protected abstract String rootPackage();

    @BeforeAll
    void loadClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(rootPackage());
    }

    /**RULE 1
     The domain must never import Spring, JPA, or Kafka.
     If the business rule for "insufficient funds" imports
     Hibernate, it is coupled to the database forever.*/
    @Test
    @DisplayName("Domain classes must not depend on Spring, JPA, or Kafka")
    void domainMustBePureJava(){
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "org.hibernate..",
                        "org.apache.kafka.."
                )
                .because(
                        "The domain contains your business rules. " +
                                "It must have zero framework dependencies. " +
                                "You must be able to test it with plain Java."
                )
                .check(classes);
    }

    /**RULE 2
     JPA @Entity belongs only in the persistence adapter.
     A domain Account and a JPA AccountEntity are different
     classes. The mapper converts between them.*/
    @Test
    @DisplayName("@Entity must only appear in persistence adapter")
    void entityOnlyInPersistenceAdapter(){
        noClasses()
                .that().areAnnotatedWith("jakarta.persistence.Entity")
                .should().resideOutsideOfPackage("..adapter.out.persistence..")
                .because(
                        "JPA annotations are infrastructure details. " +
                                "They must not appear on domain entities."
                )
                .check(classes);
    }

    /**RULE 3
    @RestController belongs only in the web adapter.
    Controllers are HTTP concerns — they have no place
    in the domain or application layer.*/
    @Test
    @DisplayName("@RestController must only appear in web adapter")
    void controllerOnlyInWebAdapter() {
        noClasses()
                .that().areAnnotatedWith(
                        "org.springframework.web.bind.annotation.RestController"
                )
                .should().resideOutsideOfPackage("..adapter.in.web..")
                .because(
                        "Controllers are HTTP adapters. " +
                                "They must not leak into the domain or application layer."
                )
                .check(classes);
    }

    /**RULE 4
    The application layer (use cases) must not depend
    on adapter classes. A use case must not import a
    JPA entity or an HTTP controller.*/
    @Test
    @DisplayName("Application layer must not depend on adapters")
    void applicationMustNotDependOnAdapters() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAPackage("..adapter..")
                .because(
                        "Use cases orchestrate domain logic. " +
                                "They must not know about HTTP or JPA. " +
                                "Dependencies always point inward."
                )
                .check(classes);
    }

    /**RULE 5
    Inbound adapters (web, messaging) must not call
    outbound adapters (persistence, external) directly.
    The controller must go through the use case.*/
    @Test
    @DisplayName("Inbound adapters must not call outbound adapters directly")
    void inboundMustNotCallOutbound() {
        noClasses()
                .that().resideInAPackage("..adapter.in..")
                .should().dependOnClassesThat()
                .resideInAPackage("..adapter.out..")
                .because(
                        "A REST controller must never reach directly into " +
                                "a JPA repository. It must go through the use case."
                )
                .check(classes);
    }

}
