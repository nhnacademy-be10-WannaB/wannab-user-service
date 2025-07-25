package shop.wannab.userservice.user.service;

import io.jsonwebtoken.Claims;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import shop.wannab.userservice.auth.dto.response.ReissueResponse;
import shop.wannab.userservice.auth.dto.response.UserResponse;
import shop.wannab.userservice.point.domain.dto.request.PointUpdateDTO;
import shop.wannab.userservice.user.domain.dto.request.UserCreateRequest;
import shop.wannab.userservice.user.domain.dto.request.UserUpdateRequest;
import shop.wannab.userservice.user.domain.dto.response.UserPageResponse;
import shop.wannab.userservice.user.domain.entity.State;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.domain.entity.UserGrade;
import shop.wannab.userservice.user.exception.RefreshTokenNotMatchException;
import shop.wannab.userservice.user.exception.UserAlreadyExistsException;
import shop.wannab.userservice.user.exception.UserNotFoundException;
import shop.wannab.userservice.user.mapper.UserMapper;
import shop.wannab.userservice.user.repository.UserGradeRepository;
import shop.wannab.userservice.user.repository.UserRepository;
import shop.wannab.userservice.utils.JwtUtil;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserGradeRepository userGradeRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REFRESH_KEY = "refresh_token:";
    private final JwtUtil jwtUtil;
    private final PostSignupService postSignupService;


    @Override
    public User createUser(UserCreateRequest userCreateDTO) {
        log.info("Service: createUser");
        if (userRepository.existsByUserLoginId(userCreateDTO.userLoginId())) {
            throw new UserAlreadyExistsException("존재하는 아이디로 회원가입 요청함");
        }

        User user = UserMapper.userCreateDtoToUser(userCreateDTO, getStandardUserGrade());
        userRepository.save(user);
        postSignupService.handlePostSignup(user);
        return user;
    }

    @Override
    public User readUser(long userId) {
        log.info("Service: readUser");
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 유저 없음"));
    }

    @Override
    public UserPageResponse readUserPageResponse(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 유저 없음"));
        return UserPageResponse.builder()
                .username(user.getUserLoginId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birth(user.getBirth())
                .nickname(user.getNickname())
                .password(user.getPassword())
                .points(user.getPoints())
                .grade(user.getUserGrade().getGradeName())
                .build();
    }

    @Override
    public UserPageResponse updateUser(long userId, UserUpdateRequest userUpdateDTO) {
        log.info("Service: updateUser");
        User user = readUser(userId);
        user.setName(userUpdateDTO.name());
        user.setEmail(userUpdateDTO.email());
        user.setPhone(userUpdateDTO.phone());
        user.setNickname(userUpdateDTO.nickname());
        user.setPassword(userUpdateDTO.password());

        userRepository.save(user);

        return UserMapper.UserToUserPageResponse(user);
    }

    @Override
    public void deleteUser(long userId) {
        log.info("Service: deleteUser");
        CheckUserExistUser(userId);
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
    public ReissueResponse reissueToken(String refreshToken) {
        log.info("Service: reissueToken");
        Claims claims = jwtUtil.parseToken(refreshToken);
        Long userId = claims.get("userId", Long.class);
        String role = claims.get("role", String.class);

        String storedRefreshToken = (String) redisTemplate.opsForHash().get(REFRESH_KEY, userId.toString());

        if (!refreshToken.equals(storedRefreshToken)) {
            throw new RefreshTokenNotMatchException("Refresh Token이 일치하지 않습니다.");
        }

        String newAccessToken = jwtUtil.createAccessToken(userId, role);
        return new ReissueResponse(newAccessToken);
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
        return userRepository.findUserIdsByBirthMonth(month);
    }

    @Override
    public UserResponse readUserResponse(String username) {
        log.info("Service: findByUsername");
        User user = userRepository.findByUserLoginId(username).
                orElseThrow(() -> new UserNotFoundException("해당하는 유저 없음"));
        return UserResponse.builder()
                .loginId(user.getUserLoginId())
                .userId(user.getUserId())
                .state(user.getState())
                .build();
    }

    @Override
    public boolean duplicated(String username) {
        Optional<User> user = userRepository.findByUserLoginId(username);
        return user.isPresent();
    }

    @Override
    public UserGrade getStandardUserGrade() {
        return userGradeRepository.findByGradeName("Standard")
                .orElseThrow(() -> new IllegalStateException("기본 등급(Standard)을 찾을 수 없습니다."));
    }

    @Override
    public void CheckUserExistUser(long userId){
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("해당하는 유저 없음");
        }
    }

}
