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

    private JwtService jwtService = new JwtService(JWT_SECRET);;

    private static final String JWT_SECRET = "RaDCFtk9mz7Uzvi3eQpCP1Y+3dJnn66NKjacD3Fwrus=";

    private static final long EXPIRATION_TIME = 1000 * 60 * 10; // 10 minutes

    @Test
    void validateToken_ShouldExtractClaimsCorrectly() {
        // Generate a test JWT token
        User user = new User();
        user.setEmail("test@example.com");
        user.setRoles("ROLE_USER,ROLE_ADMIN");
        String token = JwtUtils.createJwt(user, JWT_SECRET, EXPIRATION_TIME);

        // Act
        TokenClaims claims = jwtService.validateToken(token);

        // Assert
        assertNotNull(claims);
        assertEquals("test@example.com", claims.subject());
        assertNotNull(claims.expiresAt());
        assertNotNull(claims.issuedAt());
        assertEquals("org.beerbower", claims.issuer());
        assertEquals("ROLE_USER,ROLE_ADMIN", claims.auth());
    }
}