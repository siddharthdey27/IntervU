package com.preppilot.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtService {

    public static final String TYPE_ACCESS = "ACCESS";
    public static final String TYPE_REFRESH = "REFRESH";

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String subjectUserId, String email) {
        return buildToken(subjectUserId, email, TYPE_ACCESS, accessTokenExpirationMs);
    }

    public String generateRefreshToken(String subjectUserId, String email) {
        return buildToken(subjectUserId, email, TYPE_REFRESH, refreshTokenExpirationMs);
    }

    private String buildToken(String subjectUserId, String email, String type, long expirationMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(subjectUserId)
                .claim("email", email)
                .claim("type", type)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key())
                .compact();
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractEmail(String token) {
        return extractClaim(token, c -> c.get("email", String.class));
    }

    public String extractTokenType(String token) {
        return extractClaim(token, c -> c.get("type", String.class));
    }

    public boolean isTokenValid(String token) {
        try {
            return !extractClaim(token, Claims::getExpiration).before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        return isTokenValid(token) && TYPE_ACCESS.equals(extractTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return isTokenValid(token) && TYPE_REFRESH.equals(extractTokenType(token));
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload();
        return resolver.apply(claims);
    }
}
