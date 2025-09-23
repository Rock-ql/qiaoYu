package cn.badminton.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类：生成与解析JWT
 * 作者: xiaolei
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:badminton-secret}")
    private String secret;

    @Value("${jwt.expireMinutes:43200}") // 30天
    private long expireMinutes;

    private SecretKey getKey() {
        // jjwt 0.12+ 推荐使用 SecretKey
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String userId) {
        long now = System.currentTimeMillis();
        long exp = now + expireMinutes * 60 * 1000;
        return Jwts.builder()
                .subject(userId)
                .issuedAt(new Date(now))
                .expiration(new Date(exp))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String parseUserId(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}

