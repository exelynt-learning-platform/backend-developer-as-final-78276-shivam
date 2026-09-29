package com.example.resourcebooking.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Jwts;

class JwtServiceTest {

    private String secret;

    @BeforeEach
    void setUp() {
        SecretKey key = Jwts.SIG.HS256.key().build();

        secret = Base64.getEncoder()
                .encodeToString(key.getEncoded());
    }

    @Test
    void generateAndExtractClaims() {

        JwtService jwtService =
                new JwtService(secret, 3_600_000);

        String token =
                jwtService.generateToken("user", "USER");

        assertNotNull(token);

        assertEquals(
                "user",
                jwtService.extractUsername(token));

        assertEquals(
                "USER",
                jwtService.extractRole(token));

        assertTrue(
                jwtService.isTokenValid(token));
    }

    @Test
    void tamperedTokenIsInvalid() {

        JwtService jwtService =
                new JwtService(secret, 3_600_000);

        SecretKey anotherKey =
                Jwts.SIG.HS256.key().build();

        String anotherSecret =
                Base64.getEncoder()
                        .encodeToString(
                                anotherKey.getEncoded());

        JwtService anotherJwtService =
                new JwtService(
                        anotherSecret,
                        3_600_000);

        String token =
                anotherJwtService.generateToken(
                        "user",
                        "USER");

        assertFalse(
                jwtService.isTokenValid(token));
    }

    @Test
    void nullTokenIsInvalid() {

        JwtService jwtService =
                new JwtService(secret, 3_600_000);

        assertFalse(
                jwtService.isTokenValid(null));
    }

    @Test
    void shortSecretIsRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService(
                        "short",
                        3_600_000));
    }

    @Test
    void invalidExpirationIsRejected() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new JwtService(
                                secret,
                                0));

        assertTrue(
                exception.getMessage()
                        .contains("greater than zero"));
    }
}