package com.taxi.user.security;

import com.taxi.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiration;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String generateToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getRefId().toString())
                .claim("refId", user.getRefId())
                .claim("userId", user.getId())
                .claim("role", user.getUserType().name())
                .claim("email", user.getEmail())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(key)
                .compact();
    }

    public String generateServiceToken() {
        Date now = new Date();
        return Jwts.builder()
                .subject("service")
                .claim("role", "SERVICE")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration * 365))
                .signWith(key)
                .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    public Long extractRefId(Claims claims) {
        return claims.get("refId", Long.class);
    }

    public String extractRole(Claims claims) {
        return claims.get("role", String.class);
    }
}
