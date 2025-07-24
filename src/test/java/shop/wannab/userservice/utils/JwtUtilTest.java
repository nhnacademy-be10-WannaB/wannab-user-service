package shop.wannab.userservice.utils;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtUtilTest {


    private JwtUtil jwtUtil;
    private final String secretKeyString = "01234567890123456789012345678901"; // 최소 256bit (32바이트)
    private Key secretKey;

    @BeforeEach
    void setUp() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
        this.jwtUtil = new JwtUtil(secretKey);
    }

    @Test
    @DisplayName("Access Token 생성 및 파싱 성공")
    void createAndParseAccessToken_success() {
        // given
        Long userId = 1L;
        String role = "USER";

        // when
        String token = jwtUtil.createAccessToken(userId, role);
        Claims claims = jwtUtil.parseToken(token);

        // then
        assertThat(claims.get("userId", Long.class)).isEqualTo(userId);
        assertThat(claims.get("role", String.class)).isEqualTo(role);
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    @DisplayName("Refresh Token 생성 및 파싱 성공")
    void createAndParseRefreshToken_success() {
        // given
        Long userId = 2L;
        String role = "ADMIN";

        // when
        String token = jwtUtil.createRefreshToken(userId, role);
        Claims claims = jwtUtil.parseToken(token);

        // then
        assertThat(claims.get("userId", Long.class)).isEqualTo(userId);
        assertThat(claims.get("role", String.class)).isEqualTo(role);
        assertThat(claims.getExpiration()).isAfter(new Date());
    }
}
