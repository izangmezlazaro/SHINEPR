package com.example.demo.security;

import com.example.demo.util.AppConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

/**
 * Utilidad JWT sin Spring. Singleton inicializado en la primera carga de clase.
 * Lee la configuración desde AppConfig (db.properties / variables de entorno).
 */
public final class JwtUtil {

    private static final JwtUtil INSTANCE = new JwtUtil();

    private final SecretKey secretKey;
    private final long expirationMs;

    private JwtUtil() {
        String secret = AppConfig.jwtSecret();
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = AppConfig.jwtExpirationMs();
    }

    public static JwtUtil getInstance() {
        return INSTANCE;
    }

    public String generateToken(Integer userId, String email, String rol) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Integer getUserId(String token) {
        return Integer.valueOf(parseToken(token).getSubject());
    }

    public String getEmail(String token) {
        return parseToken(token).get("email", String.class);
    }

    public String getRol(String token) {
        return parseToken(token).get("rol", String.class);
    }
}
