package shop.wannab.userservice.user.service;

import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import shop.wannab.userservice.auth.dto.response.ReissueResponse;
import shop.wannab.userservice.auth.dto.response.UserResponse;
import shop.wannab.userservice.point.domain.dto.request.PointUpdateDTO;
import shop.wannab.userservice.point.exception.FeignClientException;
import shop.wannab.userservice.user.client.CartClient;
import shop.wannab.userservice.user.domain.dto.request.AdminUserUpdateRequest;
import shop.wannab.userservice.user.domain.dto.request.CartCreateRequest;
import shop.wannab.userservice.user.domain.dto.request.UserCreateRequest;
import shop.wannab.userservice.user.domain.dto.request.UserUpdateRequest;
import shop.wannab.userservice.user.domain.dto.response.AdminPageUserResponse;
import shop.wannab.userservice.user.domain.dto.response.UserPageResponse;
import shop.wannab.userservice.user.domain.entity.Role;
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
        log.info("action=createUser, userLoginId={}, message=\"사용자 생성 시작\"", userCreateDTO.userLoginId());
        if (userRepository.existsByUserLoginId(userCreateDTO.userLoginId())) {
            throw new UserAlreadyExistsException("존재하는 아이디로 회원가입 요청함");
        }

        User user = UserMapper.userCreateDtoToUser(userCreateDTO, getStandardUserGrade());
        userRepository.save(user);
        postSignupService.handlePostSignup(user);
        log.info("action=createUser, userId={}, message=\"사용자 생성 완료\"", user.getUserId());
        return user;
    }

    @Override
    public User readUser(long userId) {
        log.info("action=readUser, userId={}, message=\"사용자 정보 조회 시작\"", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 유저 없음"));
        log.info("action=readUser, userId={}, message=\"사용자 정보 조회 완료\"", userId);
        return user;
    }

    @Override
    public UserPageResponse readUserPageResponse(long userId) {
        log.info("action=readUserPageResponse, userId={}, message=\"사용자 페이지 정보 조회 시작\"", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 유저 없음"));
        UserPageResponse response = UserPageResponse.builder()
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
        log.info("action=readUserPageResponse, userId={}, message=\"사용자 페이지 정보 조회 완료\"", userId);
        return response;
    }

    @Override
    public UserPageResponse updateUser(long userId, UserUpdateRequest userUpdateDTO) {
        log.info("action=updateUser, userId={}, message=\"사용자 정보 업데이트 시작\"", userId);
        User user = readUser(userId);
        user.setName(userUpdateDTO.name());
        user.setEmail(userUpdateDTO.email());
        user.setPhone(userUpdateDTO.phone());
        user.setNickname(userUpdateDTO.nickname());
        user.setPassword(userUpdateDTO.password());

        userRepository.save(user);

        UserPageResponse response = UserMapper.UserToUserPageResponse(user);
        log.info("action=updateUser, userId={}, message=\"사용자 정보 업데이트 완료\"", userId);
        return response;
    }

    @Override
    public void deleteUser(long userId) {
        log.info("action=deleteUser, userId={}, message=\"사용자 삭제 시작\"", userId);
        CheckUserExistUser(userId);
        User user = userRepository.findById(userId).get();
        user.setState(State.DELETED);
        log.info("action=deleteUser, userId={}, message=\"사용자 삭제 완료\"", userId);
    }

    @Override
    public int readPoint(long userId) {
        log.info("action=readPoint, userId={}, message=\"포인트 조회 시작\"", userId);
        int points = readUser(userId).getPoints();
        log.info("action=readPoint, userId={}, points={}, message=\"포인트 조회 완료\"", userId, points);
        return points;
    }

    @Override
    public void updatePoint(long userId, PointUpdateDTO pointUpdateDTO) {
        log.info("action=updatePoint, userId={}, amount={}, message=\"포인트 업데이트 시작\"", userId, pointUpdateDTO.amount());
        readUser(userId).setPoints(pointUpdateDTO.amount());
        log.info("action=updatePoint, userId={}, amount={}, message=\"포인트 업데이트 완료\"", userId, pointUpdateDTO.amount());
    }

    @Override
    public boolean existsUser(long userId) {
        log.info("action=existsUser, userId={}, message=\"사용자 존재 여부 확인 시작\"", userId);
        boolean exists = userRepository.existsById(userId);
        log.info("action=existsUser, userId={}, exists={}, message=\"사용자 존재 여부 확인 완료\"", userId, exists);
        return exists;
    }


    @Override
    public ReissueResponse reissueToken(String refreshToken) {
        log.info("action=reissueToken, message=\"토큰 재발급 시작\"");
        Claims claims = jwtUtil.parseToken(refreshToken);
        Long userId = claims.get("userId", Long.class);
        String role = claims.get("role", String.class);

        String storedRefreshToken = (String) redisTemplate.opsForHash().get(REFRESH_KEY, userId.toString());

        if (!refreshToken.equals(storedRefreshToken)) {
            throw new RefreshTokenNotMatchException("Refresh Token이 일치하지 않습니다.");
        }

        String newAccessToken = jwtUtil.createAccessToken(userId, role);
        ReissueResponse response = new ReissueResponse(newAccessToken);
        log.info("action=reissueToken, message=\"토큰 재발급 완료\"");
        return response;
    }

    @Override
    public void logout(long userId) {
        log.info("action=logout, userId={}, message=\"로그아웃 시작\"", userId);
        redisTemplate.opsForHash().delete(REFRESH_KEY, String.valueOf(userId));
        log.info("action=logout, userId={}, message=\"로그아웃 완료\"", userId);
    }

    @Override
    public List<Long> birthUserList(int month) {
        log.info("action=birthUserList, month={}, message=\"생일 사용자 목록 조회 시작\"", month);
        if (month < 1 || month > 12) {
            log.warn("action=birthUserList, month={}, message=\"유효하지 않은 월 입력\"", month);
            throw new IllegalArgumentException("월(month)은 1~12 사이여야 합니다.");
        }
        List<Long> userIds = userRepository.findUserIdsByBirthMonth(month);
        log.info("action=birthUserList, month={}, userCount={}, message=\"생일 사용자 목록 조회 완료\"", month, userIds.size());
        return userIds;
    }

    @Override
    public UserResponse readUserResponse(String username) {
        log.info("Service: findByUsername");
        User user = userRepository.findByUserLoginId(username).
                orElseThrow(() -> new UserNotFoundException("해당하는 유저 없음"));
        return UserResponse.builder()
                .loginId(user.getUserLoginId())
                .role(user.getRole())
                .password(user.getPassword())
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
    @Override
    public Page<AdminPageUserResponse> readUserList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findAll(pageable);
        return users.map(AdminPageUserResponse::new);
    }

    @Override
    public AdminPageUserResponse readAdminPageUser(String loginId) {
        log.info("Service: readAdminPageUser");
        User user = userRepository.findByUserLoginId(loginId).orElseThrow(UserNotFoundException::new);
        return new AdminPageUserResponse(user);
    }

    @Override
    public void updateAdminUser(String loginId, AdminUserUpdateRequest adminUserUpdateRequest) {
        User user = userRepository.findByUserLoginId(loginId).orElseThrow(UserNotFoundException::new);
        user.setNickname(adminUserUpdateRequest.getNickname());
        user.setRole(Role.valueOf(adminUserUpdateRequest.getRole()));
    }

    @Override
    public void deleteAdminUser(String loginId) {
        User user = userRepository.findByUserLoginId(loginId).orElseThrow(UserNotFoundException::new);
        user.setState(State.DELETED);
    }


}
