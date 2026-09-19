package com.smartdesk.api.security;

import com.smartdesk.api.identity.model.RoleCode;
import com.smartdesk.api.identity.service.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JwtTokenServiceTest {

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void shouldIssueAndVerifyAccessToken() {
        UUID userId = UUID.randomUUID();

        AuthenticatedUser user = new AuthenticatedUser(
                userId,
                "jwt-test@example.com",
                "JWT Test User",
                Set.of(RoleCode.EMPLOYEE)
        );

        Instant beforeIssuing = Instant.now();

        IssuedToken issuedToken =
                jwtTokenService.issueAccessToken(user);

        assertFalse(issuedToken.accessToken().isBlank());
        assertEquals("Bearer", issuedToken.tokenType());
        assertTrue(issuedToken.expiresAt().isAfter(beforeIssuing));

        Jwt decodedToken =
                jwtDecoder.decode(issuedToken.accessToken());

        assertEquals(userId.toString(), decodedToken.getSubject());
        assertEquals(
                "smart-service-desk-api",
                decodedToken.getClaimAsString("iss")
        );
        assertEquals(
                "jwt-test@example.com",
                decodedToken.getClaimAsString("email")
        );
        assertEquals(
                "JWT Test User",
                decodedToken.getClaimAsString("name")
        );
        assertEquals(
                List.of("EMPLOYEE"),
                decodedToken.getClaimAsStringList("roles")
        );
        assertEquals(
                issuedToken.expiresAt(),
                decodedToken.getExpiresAt()
        );
    }
}