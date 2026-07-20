package com.bankplatform.identity.domain.model;

import java.util.Objects;

public final class Bvn {
    private static final int LENGTH = 11;
    private static final int[] WEIGHTS = {3, 7, 3, 3, 7, 3, 3, 7, 3, 3};

    private String value;

    public Bvn(String value){
        Objects.requireNonNull(value, "Bvn must not be null");
        String trimmed = value.trim();
        validate(trimmed);
        this.value = trimmed;
    }

    public String getValue(){
        return value;
    }

    public String masked(){
        return value.substring(0, 3)
                + "*".repeat(value.length() - 6)
                + value.substring(value.length() - 3);
    }

//    Validation

    private static void validate(String bvn){
        if(bvn.length() != LENGTH){
            throw new IllegalArgumentException(
                    "BVN must be exactly 11 digits, got " + bvn.length()
            );
        }
        if(!bvn.matches("\\d{11}")){
            throw new IllegalArgumentException(
                    "BVN must contain only digits"
            );
        }
    }

//    Value object contract
    @Override
    public boolean equals(Object o){
        if(this == o)return true;
        if(!(o instanceof Bvn other)) return false;
        return value.equals(other.value);
    }

    @Override
    public int hashCode(){
        return value.hashCode();
    }

    @Override
    public String toString(){
        return masked();
    }
}
