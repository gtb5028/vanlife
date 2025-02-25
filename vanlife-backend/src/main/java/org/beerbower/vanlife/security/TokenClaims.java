package org.beerbower.vanlife.security;

import java.util.Date;

/**
 * Represents the claims extracted from a JWT (JSON Web Token). It acts as a data carrier
 * for the decoded JWT payload.
 */
public record TokenClaims(
        String subject,
        Date expiresAt,
        Date issuedAt,
        String issuer,
        String auth
) {
}
