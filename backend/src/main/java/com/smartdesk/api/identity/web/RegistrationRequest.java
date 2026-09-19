package com.smartdesk.api.identity.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * JSON input accepted by the registration endpoint.
 */
public record RegistrationRequest(

        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be valid.")
        @Size(max = 254, message = "Email must not exceed 254 characters.")
        String email,

        @NotBlank(message = "Password is required.")
        @Size(
                min = 12,
                max = 64,
                message = "Password must contain between 12 and 64 characters."
        )
        String password,

        @NotBlank(message = "Full name is required.")
        @Size(max = 150, message = "Full name must not exceed 150 characters.")
        String fullName
) {
}