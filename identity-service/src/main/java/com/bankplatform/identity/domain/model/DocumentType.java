package com.bankplatform.identity.domain.model;

public enum DocumentType {
    NATIONAL_ID_FRONT,
    NATIONAL_ID_BACK,
    DRIVERS_LICENSE_FRONT,
    DRIVERS_LICENSE_BACK,
    INTERNATIONAL_PASSPORT,
    VOTERS_CARD,

    // Selfie documents for liveness and face matching
    SELFIE_WITH_ID,     // customer holding their ID next to their face
    LIVE_SELFIE         // plain selfie for face matching
}
