package shop.wannab.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import shop.wannab.userservice.auth.DoorayMessageClient;
import shop.wannab.userservice.auth.dto.request.SendMessageRequest;
import shop.wannab.userservice.auth.dto.request.TokenRequest;
import shop.wannab.userservice.auth.dto.request.UnlockRequest;
import shop.wannab.userservice.auth.dto.response.TokenResponse;
import shop.wannab.userservice.auth.service.AuthService;
import shop.wannab.userservice.user.domain.entity.State;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.repository.UserRepository;
import shop.wannab.userservice.utils.JwtUtil;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DoorayMessageClient doorayMessageClient;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Nested
    @DisplayName("login() 메서드")
    class LoginTest {
        @BeforeEach
        void setUp() {
            lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        }

        @Test
        @DisplayName("AccessToken과 RefreshToken이 발급된다")
        void login_success() {
            // given
            Long userId = 1L;
            String role = "USER";
            String accessToken = "access.token";
            String refreshToken = "refresh.token";

            TokenRequest tokenRequest = new TokenRequest(userId, role);
            when(jwtUtil.createAccessToken(userId, role)).thenReturn(accessToken);
            when(jwtUtil.createRefreshToken(userId, role)).thenReturn(refreshToken);

            // when
            TokenResponse result = authService.login(tokenRequest);

            // then
            assertThat(result.accessToken()).isEqualTo(accessToken);
            assertThat(result.refreshToken()).isEqualTo(refreshToken);
        }
    }

    @Nested
    @DisplayName("unlockRequest() 메서드")
    class UnlockRequestTest {
        @BeforeEach
        void initValueOps() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations); // 여기에만 설정
        }

        @Test
        @DisplayName("랜덤 인증 코드가 생성되어 Redis에 저장되고 메시지가 전송된다")
        void unlockRequest_success() {
            // given
            String userId = "user123";

            // when
            authService.unlockRequest(userId);

            // then
            verify(redisTemplate.opsForValue(), times(1))
                    .set(startsWith("UNLOCK_CODE:"), anyInt(), eq(3L), eq(TimeUnit.MINUTES));

            verify(doorayMessageClient, times(1)).sendUnlockCode(any(SendMessageRequest.class));
        }
    }

    @Nested
    @DisplayName("unlock() 메서드")
    class UnlockTest {
        @BeforeEach
        void initValueOps() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations); // 여기에만 설정
        }

        @Test
        @DisplayName("인증코드가 일치하면 활성화 상태로 변경하고 true를 반환")
        void unlock_success() {
            // given
            String userId = "user123";
            int code = 123456;
            String key = "UNLOCK_CODE:" + userId;

            UnlockRequest request = new UnlockRequest(userId, code);
            User user = User.standard().build();
            user.setState(State.INACTIVATE);

            when(valueOperations.get(key)).thenReturn(String.valueOf(code));
            when(userRepository.findByUserLoginId(userId)).thenReturn(Optional.of(user));

            // when
            boolean result = authService.unlock(request);

            // then
            assertThat(result).isTrue();
            assertThat(user.getState()).isEqualTo(State.ACTIVATE);
            verify(redisTemplate).delete(key);
        }

        @Test
        @DisplayName("인증코드가 일치하지 않으면 false 반환")
        void unlock_fail() {
            // given
            String userId = "user123";
            int code = 123456;
            UnlockRequest request = new UnlockRequest(userId, code);

            when(valueOperations.get("UNLOCK_CODE:" + userId)).thenReturn("999999");

            // when
            boolean result = authService.unlock(request);

            // then
            assertThat(result).isFalse();
        }
    }

}