package shop.wannab.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityManager;
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
import org.springframework.test.util.ReflectionTestUtils;
import shop.wannab.userservice.auth.dto.response.ReissueResponse;
import shop.wannab.userservice.auth.dto.response.UserResponse;
import shop.wannab.userservice.point.domain.dto.request.PointUpdateDTO;
import shop.wannab.userservice.point.exception.FeignClientException;
import shop.wannab.userservice.user.client.CartClient;
import shop.wannab.userservice.user.domain.dto.request.CartCreateRequest;
import shop.wannab.userservice.user.domain.dto.request.UserCreateRequest;
import shop.wannab.userservice.user.domain.dto.request.UserUpdateRequest;
import shop.wannab.userservice.user.domain.dto.response.UserPageResponse;
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
    private JwtUtil jwtUtil;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private HashOperations<String, Object, Object> hashOperations;
    @Mock
    private EntityManager entityManager;

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
        given(userRepository.existsByUserLoginId("testuser")).willReturn(true);

        // when & then
        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(request));
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
        ReissueResponse result = userService.reissueToken(refreshToken);

        // then
        assertThat(result.accessToken()).isEqualTo(newAccessToken);
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

        UserGrade mockGrade = new UserGrade(); // 필요한 필드가 있다면 셋팅
        User mockUser = User.standard()
                .password("pass123")
                .userLoginId("testuser")
                .name("홍길동")
                .email("test@test.com")
                .phone("010-0000-0000")
                .birth(LocalDate.of(1990, 1, 1))
                .userGrade(mockGrade)
                .build();

        // userId는 자동 생성되므로 테스트용으로 강제로 설정
        ReflectionTestUtils.setField(mockUser, "userId", userId);

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        // when
        User result = userService.readUser(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getUserLoginId()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@test.com");
        assertThat(result.getName()).isEqualTo("홍길동");
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

        UserGrade dummyGrade = new UserGrade(); // 기본 등급 객체
        User existingUser = User.standard()
                .password("oldPass")
                .userLoginId("originalUser")
                .name("Old Name")
                .email("old@test.com")
                .phone("010-1234-5678")
                .birth(LocalDate.of(1990, 1, 1))
                .userGrade(dummyGrade)
                .build();

        ReflectionTestUtils.setField(existingUser, "userId", userId);
        existingUser.setNickname("oldnick");

        UserUpdateRequest updateRequest = new UserUpdateRequest(
                "newPass", "New Name", "new@test.com", "newnick", "010-1111-2222"
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        UserPageResponse updatedUser = userService.updateUser(userId, updateRequest);

        // then
        assertThat(updatedUser.password()).isEqualTo("newPass");
        assertThat(updatedUser.name()).isEqualTo("New Name");
        assertThat(updatedUser.email()).isEqualTo("new@test.com");
        assertThat(updatedUser.phone()).isEqualTo("010-1111-2222");
        assertThat(updatedUser.nickname()).isEqualTo("newnick");
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

        UserGrade dummyGrade = new UserGrade(); // 테스트용 유저등급

        User mockUser = User.standard()
                .password("pass")
                .userLoginId("deleteTest")
                .name("홍길동")
                .email("test@test.com")
                .phone("010-0000-0000")
                .birth(LocalDate.of(1990, 1, 1))
                .userGrade(dummyGrade)
                .build();

        ReflectionTestUtils.setField(mockUser, "userId", userId);

        // 상태값은 setter로 변경
        mockUser.setState(State.ACTIVATE);

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

        UserGrade dummyGrade = new UserGrade(); // 등급은 null이면 안 되므로 더미 객체 사용

        User user = User.standard()
                .password("password")
                .userLoginId(username)
                .name("홍길동")
                .email("test@test.com")
                .phone("010-0000-0000")
                .birth(LocalDate.of(1990, 1, 1))
                .userGrade(dummyGrade)
                .build();

        ReflectionTestUtils.setField(user, "userId", 1L);
        user.setState(State.ACTIVATE);

        given(userRepository.findByUserLoginId(username)).willReturn(Optional.of(user));

        // when
        UserResponse result = userService.readUserResponse(username);

        // then
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.loginId()).isEqualTo("testuser");
        assertThat(result.state()).isEqualTo(State.ACTIVATE);
    }

    @Test
    @DisplayName("존재하지 않는 username이면 예외가 발생한다")
    void findByUsername_notFound() {
        // given
        String username = "notfound";
        given(userRepository.findByUserLoginId(username)).willReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> userService.readUserResponse(username));
    }

    @Test
    @DisplayName("이미 존재하는 username이면 true를 반환한다")
    void duplicated_true() {
        // given
        String username = "existuser";
        given(userRepository.findByUserLoginId(username)).willReturn(Optional.of(mock(User.class)));

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
        given(userRepository.findByUserLoginId(username)).willReturn(Optional.empty());

        // when
        boolean result = userService.duplicated(username);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("사용자의 포인트를 조회할 수 있다")
    void readPoint_success() {
        // given
        long userId = 1L;

        UserGrade dummyGrade = new UserGrade();
        User user = User.standard()
                .password("pw")
                .userLoginId("pointuser")
                .name("포인트맨")
                .email("point@test.com")
                .phone("010-1111-1111")
                .birth(LocalDate.of(1990, 1, 1))
                .userGrade(dummyGrade)
                .build();
        ReflectionTestUtils.setField(user, "userId", userId);
        user.setPoints(150);

        given(userRepository.findById(userId)).willReturn(java.util.Optional.of(user));

        // when
        int result = userService.readPoint(userId);

        // then
        assertThat(result).isEqualTo(150);
    }

    @Test
    @DisplayName("사용자의 포인트를 업데이트할 수 있다")
    void updatePoint_success() {
        // given
        long userId = 2L;

        UserGrade dummyGrade = new UserGrade();
        User user = User.standard()
                .password("pw")
                .userLoginId("updatepointuser")
                .name("포인트업데이터")
                .email("update@test.com")
                .phone("010-2222-2222")
                .birth(LocalDate.of(1991, 2, 2))
                .userGrade(dummyGrade)
                .build();
        ReflectionTestUtils.setField(user, "userId", userId);
        user.setPoints(100); // 기존 포인트

        PointUpdateDTO updateDTO = new PointUpdateDTO(200); // 새 포인트

        given(userRepository.findById(userId)).willReturn(java.util.Optional.of(user));

        // when
        userService.updatePoint(userId, updateDTO);

        // then
        assertThat(user.getPoints()).isEqualTo(200);
    }


    @Test
    @DisplayName("존재하는 userId로 유저 상세정보를 조회하면 UserPageResponse를 반환한다")
    void readUserPageResponse_success() {
        // given
        long userId = 1L;

        UserGrade dummyGrade = new UserGrade();
        ReflectionTestUtils.setField(dummyGrade, "gradeName", "골드");

        User user = User.standard()
                .password("securePass123")
                .userLoginId("pageuser")
                .name("홍길동")
                .email("page@test.com")
                .phone("010-3333-4444")
                .birth(LocalDate.of(1988, 5, 10))
                .userGrade(dummyGrade)
                .build();

        ReflectionTestUtils.setField(user, "userId", userId);
        user.setNickname("nick");
        user.setPoints(500);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        UserPageResponse result = userService.readUserPageResponse(userId);

        // then
        assertThat(result.username()).isEqualTo("pageuser");
        assertThat(result.name()).isEqualTo("홍길동");
        assertThat(result.email()).isEqualTo("page@test.com");
        assertThat(result.phone()).isEqualTo("010-3333-4444");
        assertThat(result.birth()).isEqualTo(LocalDate.of(1988, 5, 10));
        assertThat(result.nickname()).isEqualTo("nick");
        assertThat(result.password()).isEqualTo("securePass123");
        assertThat(result.points()).isEqualTo(500);
        assertThat(result.grade()).isEqualTo("골드");
    }

    @Test
    @DisplayName("정상적으로 유저가 생성되고, MQ와 CartClient가 호출된다")
    void createUser_success() {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "newuser", "pass123", "홍길동", "new@test.com", "010-0000-1111", LocalDate.of(1995, 5, 5)
        );

        UserGrade grade = new UserGrade();
        given(userRepository.existsByUserLoginId("newuser")).willReturn(false);
        given(userGradeRepository.findByGradeName("Standard"))
                .willReturn(Optional.of(grade));

        // 💡 핵심: save() 호출되는 실제 객체에 ID를 세팅
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "userId", 100L);
            return user;
        });

        doNothing().when(entityManager).flush();
        doNothing().when(entityManager).refresh(any(User.class));

        // when
        User result = userService.createUser(request);

        // then
        assertThat(result.getUserLoginId()).isEqualTo("newuser");
        assertThat(result.getUserId()).isEqualTo(100L);

        // MQ와 Feign 호출 검증
        then(rabbitTemplate).should().convertAndSend("wannab.user.exchange", "user.signup.event", "100");
        then(cartClient).should().createCart(new CartCreateRequest(100L));
    }


    @Test
    @DisplayName("이미 존재하는 username이면 예외를 던진다")
    void createUser_duplicateUsername() {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "duplicate", "pass", "테스터", "dup@test.com", "010-1234-5678", LocalDate.of(1990, 1, 1)
        );

        given(userRepository.existsByUserLoginId("duplicate")).willReturn(true);

        // when / then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("존재하는 아이디");
    }

    @Test
    @DisplayName("MQ 또는 CartClient 호출 중 예외 발생 시 FeignClientException 발생")
    void createUser_feignClientFail() {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "mqfail", "pw", "이벤트실패", "fail@test.com", "010-9999-9999", LocalDate.of(1991, 1, 1)
        );

        UserGrade grade = new UserGrade();
        given(userRepository.existsByUserLoginId("mqfail")).willReturn(false);
        given(userGradeRepository.findByGradeName("Standard"))
                .willReturn(Optional.of(grade));

        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "userId", 300L);
            return user;
        });

        doNothing().when(entityManager).flush();
        doNothing().when(entityManager).refresh(any(User.class));

        // MQ에서 예외 발생하도록 설정
        willThrow(new RuntimeException("MQ 실패")).given(rabbitTemplate)
                .convertAndSend(anyString(), anyString(), anyString());

        // when / then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(FeignClientException.class)
                .hasMessageContaining("MQ 실패");
    }

    @Test
    @DisplayName("Standard 등급이 존재하면 반환된다")
    void getStandardUserGrade_success() {
        // given
        UserGrade grade = new UserGrade();
        ReflectionTestUtils.setField(grade, "gradeName", "Standard");

        given(userGradeRepository.findByGradeName("Standard"))
                .willReturn(Optional.of(grade));

        // when
        UserGrade result = userService.getStandardUserGrade();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getGradeName()).isEqualTo("Standard");
    }

    @Test
    @DisplayName("Standard 등급이 없으면 예외를 던진다")
    void getStandardUserGrade_notFound() {
        // given
        given(userGradeRepository.findByGradeName("Standard"))
                .willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.getStandardUserGrade())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("기본 등급");
    }
}
