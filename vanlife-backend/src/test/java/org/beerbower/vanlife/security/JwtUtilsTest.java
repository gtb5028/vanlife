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

    private static final String SECRET_KEY = "testsecretkey12345678901234567890";
    private static final long EXPIRATION_TIME = 1000 * 60 * 10; // 10 minutes

    @Test
    void testCreateJwt() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setRoles("ROLE_USER,ROLE_ADMIN");

        // Act
        String token = JwtUtils.createJwt(user, SECRET_KEY, EXPIRATION_TIME);

        // Assert
        assertNotNull(token);

        // Parse the token to validate its contents
        Claims claims = Jwts.parser()
                .setSigningKey(new SecretKeySpec(SECRET_KEY.getBytes(), SignatureAlgorithm.HS256.getJcaName()))
                .parseClaimsJws(token)
                .getBody();

        assertEquals("test@example.com", claims.getSubject());
        assertEquals("org.beerbower", claims.getIssuer());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
        assertEquals("ROLE_USER,ROLE_ADMIN", claims.get("auth"));
    }
}
