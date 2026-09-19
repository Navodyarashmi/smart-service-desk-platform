package com.smartdesk.api.identity.web;

import com.smartdesk.api.identity.service.RegisterUserCommand;
import com.smartdesk.api.identity.service.RegisteredUser;
import com.smartdesk.api.identity.service.UserRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides public account-registration operations.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class RegistrationController {

    private final UserRegistrationService userRegistrationService;

    public RegistrationController(
            UserRegistrationService userRegistrationService
    ) {
        this.userRegistrationService = userRegistrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody RegistrationRequest request
    ) {
        RegisteredUser registeredUser = userRegistrationService.register(
                new RegisterUserCommand(
                        request.email(),
                        request.password(),
                        request.fullName()
                )
        );

        RegistrationResponse response = new RegistrationResponse(
                registeredUser.id(),
                registeredUser.email(),
                registeredUser.fullName(),
                registeredUser.role()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}