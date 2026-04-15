package com.ecommerce.user.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // ─────────────────────────────────────────────────
    // GENERATE TOKENS
    // ─────────────────────────────────────────────────

    public String generateAccessToken(
            String email,
            Long userId,
            String role) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("type", "ACCESS");

        return buildToken(claims, email, jwtExpiration);
    }

    public String generateRefreshToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "REFRESH");
        return buildToken(claims, email, refreshExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            String subject,
            long expiration) {

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(
                        System.currentTimeMillis()))
                .setExpiration(new Date(
                        System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(),
                        SignatureAlgorithm.HS256)
                .compact();
    }

    // ─────────────────────────────────────────────────
    // VALIDATE TOKEN
    // ─────────────────────────────────────────────────

    public boolean isTokenValid(
            String token, String email) {
        try {
            final String tokenEmail = extractEmail(token);
            return tokenEmail.equals(email)
                    && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation error: {}",
                    e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token)
                .before(new Date());
    }

    // ─────────────────────────────────────────────────
    // EXTRACT CLAIMS
    // ─────────────────────────────────────────────────

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token,
                claims -> claims.get("userId", Long.class));
    }

    public String extractRole(String token) {
        return extractClaim(token,
                claims -> claims.get("role", String.class));
    }

    public String extractTokenType(String token) {
        return extractClaim(token,
                claims -> claims.get("type", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token,
                Claims::getExpiration);
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64
                .decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }
}
