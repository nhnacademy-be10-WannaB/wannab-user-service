package shop.wannab.userservice.user.service;

import io.jsonwebtoken.Claims;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.userservice.auth.controller.response.UserResponse;
import shop.wannab.userservice.point.domain.dto.PointUpdateDTO;
import shop.wannab.userservice.user.client.CartClient;
import shop.wannab.userservice.user.client.CouponClient;
import shop.wannab.userservice.user.domain.dto.request.UserCreateRequest;
import shop.wannab.userservice.user.domain.dto.request.UserUpdateRequest;
import shop.wannab.userservice.user.domain.entity.State;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.exception.RefreshTokenNotMatchException;
import shop.wannab.userservice.user.exception.UserAlreadyExistsException;
import shop.wannab.userservice.user.exception.UserNotFoundException;
import shop.wannab.userservice.user.repository.UserGradeRepository;
import shop.wannab.userservice.user.repository.UserRepository;
import shop.wannab.userservice.utils.JwtUtil;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserGradeRepository userGradeRepository;
    private final CartClient cartClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CouponClient couponClient;
    private static final String REFRESH_KEY = "refresh_token:";
    private final JwtUtil jwtUtil;

    public User createUser(UserCreateRequest userCreateDTO) {
        log.info("Service: createUser");
        if (userRepository.existsByUsername(userCreateDTO.username())) {
            throw new UserAlreadyExistsException("존재하는 아이디로 회원가입 요청함");
        }
        try {
            cartClient.createCart();
        } catch (Exception e) {
        }

        User user = User.builder()
                .password(userCreateDTO.password())
                .username(userCreateDTO.username())
                .name(userCreateDTO.name())
                .email(userCreateDTO.email())
                .phone(userCreateDTO.phone())
                .birth(userCreateDTO.birth())
                .userGrade(userGradeRepository.findByGradeName("Standard"))
                .build();
        userRepository.save(user);
        couponClient.issueWelcomeCoupon(user.getUserId());
        return user;
    }

    @Override
    public User readUser(long userId) {
        log.info("Service: readUser");
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 유저 없음"));
    }

    @Override
    public User updateUser(long userId, UserUpdateRequest userupdateDTO) {
        log.info("Service: updateUser");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 유저 없음"));
        user.setName(userupdateDTO.name());
        user.setEmail(userupdateDTO.email());
        user.setPhone(userupdateDTO.phone());
        user.setNickname(userupdateDTO.nickname());
        user.setPassword(userupdateDTO.password());
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(long userId) {
        log.info("Service: deleteUser");
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("해당하는 유저 없음");
        }
        User user = userRepository.findById(userId).get();
        user.setState(State.DELETED);
    }

    @Override
    public int readPoint(long userId) {
        log.info("Service: readPoint");
        return readUser(userId).getPoints();
    }

    @Override
    public void updatePoint(long userId, PointUpdateDTO pointUpdateDTO) {
        log.info("Service: updatePoint");
        readUser(userId).setPoints(pointUpdateDTO.amount());
    }

    @Override
    public boolean existsUser(long userId) {
        log.info("Service: existsUser");
        return userRepository.existsById(userId);
    }

    @Override
    public void saveRefreshToken(String refreshToken, Long userId) {
        log.info("Service: saveRefreshToken");
        redisTemplate.opsForHash().put(REFRESH_KEY, userId.toString(), refreshToken);
    }

    @Override
    public String reissueToken(String refreshToken) {
        log.info("Service: reissueToken");
        Claims claims = jwtUtil.parseToken(refreshToken);
        Long userId = claims.get("userId", Long.class);
        String role = claims.get("role", String.class);

        String storedRefreshToken = (String) redisTemplate.opsForHash().get(REFRESH_KEY, userId.toString());

        if (!refreshToken.equals(storedRefreshToken)) {
            throw new RefreshTokenNotMatchException("Refresh Token이 일치하지 않습니다.");
        }

        String newAccessToken = jwtUtil.createAccessToken(userId, role);
        return newAccessToken;
    }

    @Override
    public void logout(long userId) {
        log.info("Service: logout");
        redisTemplate.opsForHash().delete(REFRESH_KEY, String.valueOf(userId));
    }

    @Override
    public List<Long> birthUserList(int month) {
        log.info("Service: birthUserList");
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("월(month)은 1~12 사이여야 합니다.");
        }
        List<Long> users = userRepository.findUserIdsByBirthMonth(month);
        return users;
    }

    @Override
    public UserResponse findByUsername(String username) {
        log.info("Service: findByUsername");
        User user = userRepository.findByUsername(username).
                orElseThrow(() -> new UserNotFoundException("해당하는 유저 없음"));
        UserResponse loginResponse = UserResponse.builder()
                .loginId(user.getUsername())
                .password(user.getPassword())
                .userId(user.getUserId())
                .state(user.getState())
                .role(user.getRole())
                .build();
        return loginResponse;
    }

    @Override
    public boolean duplicated(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return true;
        }
        return false;
    }

    //TODO 배포전 삭제
    @Override
    public User human(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setState(State.INACTIVATE);
        return user;
    }

}
