package com.bankplatform.shared.logging;

public final class MaskingUtil {
    private MaskingUtil(){
    }

    public static final String REDACTED = "[REDACTED]";

    public static String maskCardNumber(String cardNumber){
        if (cardNumber == null) return null;
        String digits = cardNumber.replaceAll("[\\s-]", "");
        if (digits.length() < 4) return "****";
        return "****" + digits.substring(digits.length() - 4);
    }

    public static String maskBvn(String bvn){
        if(bvn == null) return null;
        if(bvn.length() < 6) return "***";
        return bvn.substring(0, 3) + "*".repeat(bvn.length() - 6) + bvn.substring(bvn.length() - 3);
    }

    public static String maskNuban(String nuban) {
        if (nuban == null) return null;
        if (nuban.length() < 4) return "****";
        return "*".repeat(nuban.length() - 4) + nuban.substring(nuban.length() - 4);
    }

    public static String maskEmail(String email) {
        if (email == null) return null;
        int atIndex = email.indexOf('@');
        if (atIndex < 0) return "***";
        return "***" + email.substring(atIndex);
    }

    public static String maskPhone(String phone) {
        if (phone == null) return null;
        if (phone.length() < 4) return "****";
        String prefix = phone.startsWith("+234") ? "+234" : "";
        int    maskStart = prefix.length();
        return prefix + "*".repeat(Math.max(0, phone.length() - maskStart - 4)) + phone.substring(phone.length() - 4);
    }
}
