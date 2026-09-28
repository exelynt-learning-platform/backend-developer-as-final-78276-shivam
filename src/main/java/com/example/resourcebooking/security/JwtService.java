package com.example.resourcebooking.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String secret;
    private final long expirationMillis;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:3600000}") long expirationMillis) {

        this.secret = secret;
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(String username, String role) {

        long issuedAt = System.currentTimeMillis();
        long expiresAt = issuedAt + expirationMillis;

        String header = base64Url(
                "{\"alg\":\"HS256\",\"typ\":\"JWT\"}");

        String payload = base64Url(
                "{\"sub\":\"" + escapeJson(username)
                        + "\",\"role\":\"" + escapeJson(role)
                        + "\",\"iat\":" + issuedAt
                        + ",\"exp\":" + expiresAt + "}");

        String content = header + "." + payload;
        String signature = sign(content);

        return content + "." + signature;
    }

    public String extractUsername(String token) {
        return parseClaims(token).get("sub");
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role");
    }

    public boolean isTokenValid(String token) {

        try {
            Map<String, String> claims = parseClaims(token);
            String exp = claims.get("exp");

            return exp != null
                    && Long.parseLong(exp) > System.currentTimeMillis();

        } catch (RuntimeException ex) {
            return false;
        }
    }

    private Map<String, String> parseClaims(String token) {

        Objects.requireNonNull(token, "Token cannot be null");

        String[] parts = token.split("\\.", -1);

        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT");
        }

        String expectedSignature =
                sign(parts[0] + "." + parts[1]);

        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8))) {

            throw new IllegalArgumentException(
                    "Invalid JWT signature");
        }

        String payload = new String(
                Base64.getUrlDecoder().decode(parts[1]),
                StandardCharsets.UTF_8);

        String subject = jsonValue(payload, "sub");
        String role = jsonValue(payload, "role");
        String exp = jsonValue(payload, "exp");

        return Map.of(
                "sub", subject,
                "role", role,
                "exp", exp);
    }

    private String sign(String content) {

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);

            SecretKeySpec key = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM);

            mac.init(key);

            byte[] signature = mac.doFinal(
                    content.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(signature);

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Could not sign JWT", ex);
        }
    }

    private String base64Url(String value) {

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        value.getBytes(StandardCharsets.UTF_8));
    }

    private String jsonValue(String json, String key) {

        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);

        if (start < 0) {
            throw new IllegalArgumentException(
                    "Missing JWT claim: " + key);
        }

        start += pattern.length();

        if (start < json.length()
                && json.charAt(start) == '"') {

            start++;

            int end = json.indexOf('"', start);

            if (end < 0) {
                throw new IllegalArgumentException(
                        "Invalid JWT claim");
            }

            return json.substring(start, end);
        }

        int end = start;

        while (end < json.length()
                && json.charAt(end) != ','
                && json.charAt(end) != '}') {

            end++;
        }

        return json.substring(start, end);
    }

    private String escapeJson(String value) {

        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
