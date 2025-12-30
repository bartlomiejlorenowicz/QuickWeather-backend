package com.quickweather.security;

import com.quickweather.domain.user.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Profile("test")
public class JwtTestUtil {

    private Key key;
    private Key resetKey;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.reset-secret}")
    private String resetSecret;

    @PostConstruct
    public void init() {
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        key = Keys.hmacShaKeyFor(decodedKey);

        byte[] decodedResetKey = Base64.getDecoder().decode(resetSecret);
        resetKey = Keys.hmacShaKeyFor(decodedResetKey);
    }

    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 godzina
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateResetToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId())
                .claim("type", "reset-password")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 min ważności
                .signWith(resetKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("token validation failed");
        }
    }

    public boolean validateResetToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(resetKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            throw new RuntimeException("reset token validation failed");
        }
    }

    public Key getResetKey() {
        return resetKey;
    }
}
