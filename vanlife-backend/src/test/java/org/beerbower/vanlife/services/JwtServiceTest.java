package org.beerbower.vanlife.services;

import org.beerbower.vanlife.entities.User;
import org.beerbower.vanlife.security.JwtUtils;
import org.beerbower.vanlife.security.TokenClaims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@TestPropertySource("classpath:application.properties")
class JwtServiceTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_ROLES = "ROLE_USER,ROLE_ADMIN";
    private static final String TEST_JWT_SECRET = "testsecretkey12345678901234567890";
    private static final String TEST_ISSUER = "test.issuer";
    private static final long EXPIRATION_TIME = 1000 * 60 * 10; // 10 minutes

    private final JwtService jwtService = new JwtService(TEST_JWT_SECRET);;

    @Test
    void validateToken_ShouldExtractClaimsCorrectly() {
        // Generate a test JWT token
        User user = new User();
        user.setEmail(TEST_EMAIL);
        user.setRoles(TEST_ROLES);
        String token = JwtUtils.createJwt(user, TEST_JWT_SECRET, TEST_ISSUER, EXPIRATION_TIME);

        // Act
        TokenClaims claims = jwtService.validateToken(token);

        // Assert
        assertNotNull(claims);
        assertEquals(TEST_EMAIL, claims.subject());
        assertNotNull(claims.expiresAt());
        assertNotNull(claims.issuedAt());
        assertEquals(TEST_ISSUER, claims.issuer());
        assertEquals(TEST_ROLES, claims.auth());
    }
}