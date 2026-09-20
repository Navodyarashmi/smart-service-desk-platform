package com.smartdesk.api.identity.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Provides information about the authenticated account.
 */
@RestController
@RequestMapping("/api/v1/users")
public class CurrentUserController {

    @GetMapping("/me")
    public CurrentUserResponse currentUser(
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<String> roles = jwt.getClaimAsStringList("roles");

        return new CurrentUserResponse(
                UUID.fromString(jwt.getSubject()),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("name"),
                List.copyOf(roles)
        );
    }
}