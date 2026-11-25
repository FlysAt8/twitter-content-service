package com.twitter.contentservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Optional<Claims> parseClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Optional<UUID> parseUserId(String token) {
        return parseClaims(token).flatMap(claims -> {
            try {
                return Optional.of(UUID.fromString(claims.getSubject()));
            } catch (IllegalArgumentException | NullPointerException e) {
                return Optional.empty();
            }
        });
    }

    public List<String> parseRoles(String token) {
        return parseClaims(token)
                .map(claims -> {
                    Object roles = claims.get("roles");

                    if (!(roles instanceof List<?> list)) {
                        return List.<String>of();
                    }

                    return list.stream()
                            .filter(String.class::isInstance)
                            .map(String.class::cast)
                            .toList();
                })
                .orElse(List.of());
    }
}
