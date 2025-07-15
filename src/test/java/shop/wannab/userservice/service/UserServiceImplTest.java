package shop.wannab.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.jsonwebtoken.Claims;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import shop.wannab.userservice.auth.controller.response.UserResponse;
import shop.wannab.userservice.user.client.CartClient;
import shop.wannab.userservice.user.client.CouponClient;
import shop.wannab.userservice.user.domain.dto.request.UserCreateRequest;
import shop.wannab.userservice.user.domain.dto.request.UserUpdateRequest;
import shop.wannab.userservice.user.domain.entity.Role;
import shop.wannab.userservice.user.domain.entity.State;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.domain.entity.UserGrade;
import shop.wannab.userservice.user.exception.RefreshTokenNotMatchException;
import shop.wannab.userservice.user.exception.UserAlreadyExistsException;
import shop.wannab.userservice.user.exception.UserNotFoundException;
import shop.wannab.userservice.user.repository.UserGradeRepository;
import shop.wannab.userservice.user.repository.UserRepository;
import shop.wannab.userservice.user.service.UserServiceImpl;
import shop.wannab.userservice.utils.JwtUtil;

@DisplayName("UserService 테스트")
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserGradeRepository userGradeRepository;
    @Mock
    private CartClient cartClient;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private CouponClient couponClient;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }


    @Test
    @DisplayName("이미 존재하는 username이면 예외를 던진다")
    void createUser_alreadyExists_throwsException() {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "testuser", "password", "홍길동", "test@email.com", "01012345678", LocalDate.of(1990, 1, 1)
        );
        given(userRepository.existsByUsername("testuser")).willReturn(true);

        // when & then
        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(request));
    }

    @Test
    @DisplayName("정상적인 회원가입 시 모든 외부 의존 호출이 발생하고 User가 반환된다")
    void createUser_success() {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "testuser", "password", "홍길동", "test@email.com", "01012345678", LocalDate.of(1990, 1, 1)
        );

        given(userRepository.existsByUsername("testuser")).willReturn(false);
        given(userGradeRepository.findByGradeName("Standard")).willReturn(mock(UserGrade.class));

        User savedUser = User.builder()
                .userId(1L)
                .username("testuser")
                .password("password")
                .name("홍길동")
                .email("test@email.com")
                .phone("01012345678")
                .birth(LocalDate.of(1990, 1, 1))
                .userGrade(mock(UserGrade.class))
                .build();
        given(userRepository.save(any(User.class)))
                .willAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    Field field = User.class.getDeclaredField("userId");
                    field.setAccessible(true);
                    field.set(user, 1L);
                    return user;
                });

        // when
        User result = userService.createUser(request);

        // then
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(cartClient).createCart();
        verify(couponClient).issueWelcomeCoupon(1L);
        verify(rabbitTemplate).convertAndSend("wannab.user.exchange", "user.signup.event", 1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("정상적인 RefreshToken이면 새로운 AccessToken을 반환한다")
    void reissueToken_success() {
        // given
        String refreshToken = "valid-refresh-token";
        Long userId = 1L;
        String role = "USER";
        String newAccessToken = "new-access-token";

        Claims claims = mock(Claims.class);
        given(claims.get("userId", Long.class)).willReturn(userId);
        given(claims.get("role", String.class)).willReturn(role);
        given(jwtUtil.parseToken(refreshToken)).willReturn(claims);
        given(redisTemplate.opsForHash().get("refresh_token:", "1")).willReturn(refreshToken);
        given(jwtUtil.createAccessToken(userId, role)).willReturn(newAccessToken);

        // when
        String result = userService.reissueToken(refreshToken);

        // then
        assertThat(result).isEqualTo(newAccessToken);
    }

    @Test
    @DisplayName("RefreshToken이 Redis와 일치하지 않으면 예외 발생")
    void reissueToken_tokenMismatch_throwsException() {
        // given
        String requestToken = "request-token";
        String storedToken = "stored-different-token";
        Long userId = 1L;
        String role = "USER";

        Claims claims = mock(Claims.class);
        given(claims.get("userId", Long.class)).willReturn(userId);
        given(claims.get("role", String.class)).willReturn(role);
        given(jwtUtil.parseToken(requestToken)).willReturn(claims);
        given(redisTemplate.opsForHash().get("refresh_token:", "1")).willReturn(storedToken);

        // when & then
        assertThrows(RefreshTokenNotMatchException.class, () -> userService.reissueToken(requestToken));
    }

    @Test
    @DisplayName("Redis에 저장된 RefreshToken이 null이면 예외 발생")
    void reissueToken_nullToken_throwsException() {
        // given
        String requestToken = "request-token";
        Long userId = 1L;
        String role = "USER";

        Claims claims = mock(Claims.class);
        given(claims.get("userId", Long.class)).willReturn(userId);
        given(claims.get("role", String.class)).willReturn(role);
        given(jwtUtil.parseToken(requestToken)).willReturn(claims);
        given(redisTemplate.opsForHash().get("refresh_token:", "1")).willReturn(null);

        // when & then
        assertThrows(RefreshTokenNotMatchException.class, () -> userService.reissueToken(requestToken));
    }

    @Test
    @DisplayName("존재하는 userId면 User 객체를 반환한다")
    void readUser_success() {
        // given
        Long userId = 1L;
        User mockUser = User.builder()
                .userId(userId)
                .username("testuser")
                .password("pass")
                .name("홍길동")
                .email("test@test.com")
                .phone("010-0000-0000")
                .birth(LocalDate.of(1990, 1, 1))
                .state(State.ACTIVATE)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        // when
        User result = userService.readUser(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("존재하지 않는 userId면 UserNotFoundException 예외 발생")
    void readUser_notFound_throwsException() {
        // given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> userService.readUser(userId));
    }

    @Test
    @DisplayName("존재하는 유저라면 정보를 수정하고 저장한다")
    void updateUser_success() {
        // given
        Long userId = 1L;
        User existingUser = User.builder()
                .userId(userId)
                .username("originalUser")
                .password("oldPass")
                .name("Old Name")
                .email("old@test.com")
                .phone("010-1234-5678")
                .nickname("oldnick")
                .build();

        UserUpdateRequest updateRequest = new UserUpdateRequest(
                "newPass", "New Name", "new@test.com", "newnick", "010-1111-2222"
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        User updatedUser = userService.updateUser(userId, updateRequest);

        // then
        assertThat(updatedUser.getPassword()).isEqualTo("newPass");
        assertThat(updatedUser.getName()).isEqualTo("New Name");
        assertThat(updatedUser.getEmail()).isEqualTo("new@test.com");
        assertThat(updatedUser.getPhone()).isEqualTo("010-1111-2222");
        assertThat(updatedUser.getNickname()).isEqualTo("newnick");
    }

    @Test
    @DisplayName("존재하지 않는 유저라면 UserNotFoundException을 발생시킨다")
    void updateUser_userNotFound() {
        // given
        Long userId = 999L;
        UserUpdateRequest updateRequest = new UserUpdateRequest("pw", "name", "email", "phone", "nick");

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(userId, updateRequest));
    }

    @Test
    @DisplayName("존재하는 유저라면 상태를 DELETED로 변경한다")
    void deleteUser_success() {
        // given
        Long userId = 1L;
        User mockUser = User.builder()
                .userId(userId)
                .username("deleteTest")
                .password("pass")
                .state(State.ACTIVATE)
                .build();

        given(userRepository.existsById(userId)).willReturn(true);
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        // when
        userService.deleteUser(userId);

        // then
        assertThat(mockUser.getState()).isEqualTo(State.DELETED);
    }

    @Test
    @DisplayName("존재하지 않는 유저라면 예외가 발생한다")
    void deleteUser_userNotFound() {
        // given
        Long userId = 999L;
        given(userRepository.existsById(userId)).willReturn(false);

        // when & then
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));
    }

    @Test
    @DisplayName("정상적인 월이면 유저 ID 리스트를 반환한다")
    void birthUserList_success() {
        // given
        int month = 3;
        List<Long> mockUserIds = List.of(1L, 2L, 3L);
        given(userRepository.findUserIdsByBirthMonth(month)).willReturn(mockUserIds);

        // when
        List<Long> result = userService.birthUserList(month);

        // then
        assertThat(result).hasSize(3).containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("월이 0이면 IllegalArgumentException이 발생한다")
    void birthUserList_invalidMonth_low() {
        // given
        int invalidMonth = 0;

        // when & then
        assertThrows(IllegalArgumentException.class, () -> userService.birthUserList(invalidMonth));
    }

    @Test
    @DisplayName("월이 13이면 IllegalArgumentException이 발생한다")
    void birthUserList_invalidMonth_high() {
        // given
        int invalidMonth = 13;

        // when & then
        assertThrows(IllegalArgumentException.class, () -> userService.birthUserList(invalidMonth));
    }

    @Test
    @DisplayName("존재하는 userId면 true를 반환한다")
    void existsUser_true() {
        // given
        Long userId = 1L;
        given(userRepository.existsById(userId)).willReturn(true);

        // when
        boolean exists = userService.existsUser(userId);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 userId면 false를 반환한다")
    void existsUser_false() {
        // given
        Long userId = 999L;
        given(userRepository.existsById(userId)).willReturn(false);

        // when
        boolean exists = userService.existsUser(userId);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("로그아웃 시 refreshToken이 Redis에서 삭제된다")
    void logout_success() {
        // given
        Long userId = 1L;
        HashOperations<String, Object, Object> hashOperations = mock(HashOperations.class);
        given(redisTemplate.opsForHash()).willReturn(hashOperations);

        // when
        userService.logout(userId);

        // then
        verify(hashOperations).delete("refresh_token:", String.valueOf(userId));
    }

    @Test
    @DisplayName("존재하는 username이면 UserResponse를 반환한다")
    void findByUsername_success() {
        // given
        String username = "testuser";
        User user = User.builder()
                .userId(1L)
                .username(username)
                .password("password")
                .state(State.ACTIVATE)
                .role(Role.USER)
                .build();

        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));

        // when
        UserResponse result = userService.findByUsername(username);

        // then
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.loginId()).isEqualTo("testuser");
        assertThat(result.state()).isEqualTo(State.ACTIVATE);
        assertThat(result.role()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("존재하지 않는 username이면 예외가 발생한다")
    void findByUsername_notFound() {
        // given
        String username = "notfound";
        given(userRepository.findByUsername(username)).willReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> userService.findByUsername(username));
    }

    @Test
    @DisplayName("이미 존재하는 username이면 true를 반환한다")
    void duplicated_true() {
        // given
        String username = "existuser";
        given(userRepository.findByUsername(username)).willReturn(Optional.of(mock(User.class)));

        // when
        boolean result = userService.duplicated(username);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 username이면 false를 반환한다")
    void duplicated_false() {
        // given
        String username = "newuser";
        given(userRepository.findByUsername(username)).willReturn(Optional.empty());

        // when
        boolean result = userService.duplicated(username);

        // then
        assertThat(result).isFalse();
    }
}
