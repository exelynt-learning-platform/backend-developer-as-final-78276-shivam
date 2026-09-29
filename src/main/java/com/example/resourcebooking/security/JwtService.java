package com.example.resourcebooking.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration:86400000}") long expirationMillis) {

        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT secret must not be empty");
        }

        if (expirationMillis <= 0) {
            throw new IllegalArgumentException(
                    "JWT expiration must be greater than zero");
        }

        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (secretBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain at least 32 bytes for HS256");
        }

        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(String username, String role) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be empty");
        }

        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role must not be empty");
        }

        Date issuedAt = new Date();
        Date expiration = new Date(
                issuedAt.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        String username = parseClaims(token).getSubject();

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("JWT subject is missing");
        }

        return username;
    }

    public String extractRole(String token) {
        String role = parseClaims(token).get("role", String.class);

        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("JWT role claim is missing");
        }

        return role;
    }

    public boolean isTokenValid(String token) {

        try {
            Claims claims = parseClaims(token);

            String username = claims.getSubject();
            Date expiration = claims.getExpiration();

            return username != null
                    && !username.isBlank()
                    && expiration != null
                    && expiration.after(new Date());

        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT token must not be empty");
        }

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
