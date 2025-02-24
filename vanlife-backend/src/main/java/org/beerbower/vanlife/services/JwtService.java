package org.beerbower.vanlife.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.beerbower.vanlife.security.TokenClaims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

/**
 * Responsible for validating JWT tokens and extracting the claims.
 */
@Service
public class JwtService {
    private final String jwtSecret;

    // Constructor injection
    public JwtService(@Value("${jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public TokenClaims validateToken(String token) {
        Key key = new SecretKeySpec(jwtSecret.getBytes(), SignatureAlgorithm.HS256.getJcaName());

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return new TokenClaims(
                claims.get("sub", String.class),
                claims.get("exp", Date.class),
                claims.get("iat", Date.class),
                claims.get("iss", String.class),
                claims.get("auth", String.class)
        );
    }
}
