package fr.umfds.spmanager.security;

import fr.umfds.spmanager.config.AppConfig;
import fr.umfds.spmanager.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    private static SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(AppConfig.getJwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(int userId, String login, Role role) {
        long expirationTimeMs = 86400000; // 24 heures
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTimeMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("login", login)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public static AuthenticatedUser validateToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            int userId = claims.get("userId", Integer.class);
            String login = claims.get("login", String.class);
            String roleStr = claims.get("role", String.class);
            Role role = Role.valueOf(roleStr);

            return new AuthenticatedUser(userId, login, role);
        } catch (Exception e) {
            return null;
        }
    }
}
