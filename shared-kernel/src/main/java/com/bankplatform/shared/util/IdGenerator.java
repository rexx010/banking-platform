package com.bankplatform.shared.util;

import java.util.UUID;

public final class IdGenerator {
    private IdGenerator(){}

    public static String generate(){
        return UUID.randomUUID().toString();
    }

    public static String generateCompact(){
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String prefixed(String prefix){
        return prefix + "_" + generateCompact().substring(0, 16);
    }
}
