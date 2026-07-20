package com.bankplatform.account.adapter.in.web.dto.request;

import jakarta.validation.constraints.*;

public final class AccountRequests {

    private AccountRequests() {}

    public record OpenAccountRequest(

            @NotBlank(message = "BVN is required")
            @Pattern(regexp = "\\d{11}",
                    message = "BVN must be exactly 11 digits")
            String bvn,

            @NotBlank(message = "Bank code is required")
            @Pattern(regexp = "\\d{3}",
                    message = "Bank code must be exactly 3 digits")
            String bankCode,

            @NotNull(message = "Account type is required")
            String accountType,   // SAVINGS, CURRENT, DOMICILIARY

            @NotBlank(message = "Currency is required")
            @Pattern(regexp = "[A-Z]{3}",
                    message = "Currency must be a 3-letter ISO code e.g. NGN")
            String currency

    ) {}
}