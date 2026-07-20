package com.bankplatform.identity.adapter.in.web.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public final class IdentityRequests {

    private IdentityRequests() {}

    public record CreateBvnRequest(

            @NotBlank(message = "First name is required")
            @Size(max = 100)
            String firstName,

            @NotBlank(message = "Last name is required")
            @Size(max = 100)
            String lastName,

            @Size(max = 100)
            String middleName,

            @NotNull(message = "Date of birth is required")
            @Past(message = "Date of birth must be in the past")
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate dateOfBirth,

            @Pattern(
                    regexp  = "^[0-9]{11}$",
                    message = "NIN must be exactly 11 digits"
            )
            String nin,

            @NotBlank(message = "Phone number is required")
            @Pattern(
                    regexp  = "^(\\+234|0)[789][01]\\d{8}$",
                    message = "Must be a valid Nigerian phone number"
            )
            String phoneNumber,

            @Email(message = "Must be a valid email address")
            String email,

            @Size(max = 500)
            String address,

            @Size(max = 100)
            String stateOfOrigin

    ) {}
}