package org.beerbower.vanlife.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.beerbower.vanlife.entities.User;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

/**
 * Utility class for generating a JWT (JSON Web Token).
 */
public class JwtUtils {

    public static String createJwt(User user, String jwtSecret, long jwtExpiration) {
        Key key = new SecretKeySpec(jwtSecret.getBytes(), SignatureAlgorithm.HS256.getJcaName());

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuer("org.beerbower")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .claim("auth", user.getRoles())
                .compact();
    }
}
