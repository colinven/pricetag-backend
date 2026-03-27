package com.pricetag.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegistrationRequest(

        // company fields
        @NotBlank String companyName,
        @Email @NotBlank String companyEmail,
        @NotBlank String companyPhone,

        // user fields
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        @NotBlank String password

){}
