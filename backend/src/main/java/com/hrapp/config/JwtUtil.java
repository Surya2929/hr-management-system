package com.hrapp.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")     private String secret;
    @Value("${app.jwt.expiration-ms}") private long expirationMs;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails user) {
        Map<String,Object> claims = new HashMap<>();
        claims.put("role", user.getAuthorities().iterator().next().getAuthority());
        return Jwts.builder()
                .claims(claims).subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getKey()).compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // True if the token was created before the password was last changed (so it must be rejected).
    // Compared in whole seconds because the token's "issued at" time has no milliseconds.
    public boolean isIssuedBefore(String token, LocalDateTime passwordChangedAt) {
        if (passwordChangedAt == null) return false;
        Date issuedAt = extractClaim(token, Claims::getIssuedAt);
        long changedAtSeconds = passwordChangedAt.atZone(ZoneId.systemDefault()).toEpochSecond();
        return issuedAt.toInstant().getEpochSecond() < changedAtSeconds;
    }

    public boolean validateToken(String token, UserDetails user) {
        return extractUsername(token).equals(user.getUsername()) && !isExpired(token);
    }

    private boolean isExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims,T> fn) {
        Claims claims = Jwts.parser().verifyWith(getKey()).build()
                .parseSignedClaims(token).getPayload();
        return fn.apply(claims);
    }
}
