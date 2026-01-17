package com.zyloerp.core.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        // Garante que a chave tenha tamanho suficiente para HS256 (mínimo 32 bytes)
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateTokenFromUsername(userDetails.getUsername());
    }

    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        // Na versão 0.12.x, usamos subject(), issuer(), expiration(), etc.
        // E o signWith não precisa mais do segundo argumento se a chave já for do tipo correto
        return Jwts.builder()
                .subject(username)
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            System.err.println("Erro na validação do Token: " + ex.getMessage());
        }
        return false;
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaims(token).getExpiration();
    }

    // Helper para evitar repetição de código
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey) // Mudou de setSigningKey para verifyWith
                .build()
                .parseSignedClaims(token) // Mudou de parseClaimsJws para parseSignedClaims
                .getPayload(); // Mudou de getBody para getPayload
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}