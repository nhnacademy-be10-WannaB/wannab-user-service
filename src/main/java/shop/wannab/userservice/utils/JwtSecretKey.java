package shop.wannab.userservice.utils;

import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtSecretKey {
    @Value("${jwt.secret-key}")
    private String secret;

    @Bean
    public Key jwtSigningKey() {
        if (secret == null) {
            throw new IllegalArgumentException("JWT secret is null!");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

}
