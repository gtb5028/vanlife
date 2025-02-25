package org.beerbower.vanlife.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.beerbower.vanlife.entities.User;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtils.
 */
class JwtUtilsTest {

    private static final long EXPIRATION_TIME = 1000 * 60 * 10; // 10 minutes
    private static final String TEST_SECRET_KEY = "testsecretkey12345678901234567890";
    private static final String TEST_ISSUER = "test.issuer";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_ROLES = "ROLE_USER,ROLE_ADMIN";
    private static final String AUTH_CLAIMS = "auth";

    @Test
    void testCreateJwt() {
        // Arrange
        User user = new User();
        user.setEmail(TEST_EMAIL);
        user.setRoles(TEST_ROLES);

        // Act
        String token = JwtUtils.createJwt(user, TEST_SECRET_KEY, TEST_ISSUER, EXPIRATION_TIME);

        // Assert
        assertNotNull(token);

        // Parse the token to validate its contents
        Claims claims = Jwts.parser()
                .setSigningKey(new SecretKeySpec(TEST_SECRET_KEY.getBytes(), SignatureAlgorithm.HS256.getJcaName()))
                .parseClaimsJws(token)
                .getBody();

        assertEquals(TEST_EMAIL, claims.getSubject());
        assertEquals(TEST_ISSUER, claims.getIssuer());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
        assertEquals(TEST_ROLES, claims.get(AUTH_CLAIMS));
    }
}
