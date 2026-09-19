package com.smartdesk.api.identity.web;

import com.smartdesk.api.identity.service.AuthenticatedUser;
import com.smartdesk.api.identity.service.LoginCommand;
import com.smartdesk.api.identity.service.UserAuthenticationService;
import com.smartdesk.api.security.IssuedToken;
import com.smartdesk.api.security.JwtTokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides public login operations.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {

    private final UserAuthenticationService authenticationService;
    private final JwtTokenService jwtTokenService;

    public LoginController(
            UserAuthenticationService authenticationService,
            JwtTokenService jwtTokenService
    ) {
        this.authenticationService = authenticationService;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthenticatedUser authenticatedUser =
                authenticationService.authenticate(
                        new LoginCommand(
                                request.email(),
                                request.password()
                        )
                );

        IssuedToken issuedToken =
                jwtTokenService.issueAccessToken(authenticatedUser);

        LoginResponse response = new LoginResponse(
                issuedToken.accessToken(),
                issuedToken.tokenType(),
                issuedToken.expiresAt(),
                authenticatedUser.id(),
                authenticatedUser.email(),
                authenticatedUser.fullName(),
                authenticatedUser.roles()
        );

        return ResponseEntity.ok(response);
    }
}