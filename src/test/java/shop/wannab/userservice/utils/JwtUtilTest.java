package shop.wannab.userservice.utils;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private final String secretKey = "01234567890123456789012345678901"; // 32바이트 이상 HMAC 키 필요

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(secretKey);
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
        assertThat(claims.getExpiration()).isAfter(new java.util.Date());
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
        assertThat(claims.getExpiration()).isAfter(new java.util.Date());
    }

    @Test
    @DisplayName("JWT 키가 null일 경우 예외 발생")
    void constructor_throwException_whenSecretNull() {
        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new JwtUtil(null);
        });
    }
}
