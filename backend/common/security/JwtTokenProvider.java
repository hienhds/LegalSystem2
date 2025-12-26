package com.example.backend.common.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long jwtExpirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration.access}") long jwtExpirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = jwtExpirationMs;
    }

    // sinh 1 jwt co subject = uname , exp = now + jwtExpiration

    public String generateToken(String username){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Lấy username (subject) từ token đã ký.
     * Nếu token invalid, hàm này sẽ ném JwtException.
     */
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // kiem tra token co hop le khoong (chu ky vaf expiration)

    public boolean validateToken(String token){
        try{
            Jwts.parser().setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            return true;
        }
        catch (JwtException | IllegalArgumentException ex){
            return false;
        }
    }

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }
}
