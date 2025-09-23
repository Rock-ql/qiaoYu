package cn.badminton.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
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
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        // HS256 需要 >= 256 bits(32 bytes)。若不足则以 SHA-256 派生稳定密钥，避免 WeakKeyException
        if (raw.length < 32) {
            try {
                raw = MessageDigest.getInstance("SHA-256").digest(raw);
            } catch (NoSuchAlgorithmException ignored) {
                raw = Arrays.copyOf(raw, 32);
            }
        }
        return Keys.hmacShaKeyFor(raw);
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
