package shop.wannab.userservice.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class JwtUtil {
    
    private static final long EXPIRATION_TIME = System.currentTimeMillis() + 15 * 60 * 1000;
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000;
    private final Key secretKey;

    public String createAccessToken(Long userId, String role) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long userId, String role) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(REFRESH_TOKEN_EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
