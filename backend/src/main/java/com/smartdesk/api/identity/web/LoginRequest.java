package com.smartdesk.api.identity.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Login credentials received from the frontend.
 */
public record LoginRequest(

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
        String password
) {
}