package shop.wannab.userservice.user.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.userservice.user.domain.dto.UserCreateDTO;
import shop.wannab.userservice.user.domain.dto.UserUpdateDTO;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.exception.RefreshTokenNotMatchException;
import shop.wannab.userservice.user.exception.UserAlreadyExistsException;
import shop.wannab.userservice.user.exception.UserNotFoundException;
import shop.wannab.userservice.user.exception.UsernameOrPasswordMismatchException;
import shop.wannab.userservice.user.repository.UserRepository;
import shop.wannab.userservice.utils.JwtUtil;
import shop.wannab.userservice.utils.Util;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REFRESH_KEY = "refresh_token:";

    public User createUser(UserCreateDTO userCreateDTO) {
        if (userRepository.existsByUsername(userCreateDTO.username())) {
            throw new UserAlreadyExistsException("존재하는 아이디로 회원가입 요청함");
        }

        User user = User.builder()
                .password(userCreateDTO.password())
                .username(userCreateDTO.username())
                .name(userCreateDTO.name())
                .email(userCreateDTO.email())
                .phone(userCreateDTO.phone())
                .birth(userCreateDTO.birth())
                .build();
        return userRepository.save(user);
    }

    @Override
    public User readUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 유저 없음"));
    }

    @Override
    public User updateUser(long userId, UserUpdateDTO userupdateDTO) {
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
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("해당하는 유저 없음");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException();
        }
        if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
            return user;
        } else {
            throw new UsernameOrPasswordMismatchException("사용자 정보가 일치하지 않습니다.");
        }
    }

    @Override
    public String generateAccessToken(long userId, String userRole) {
        return JwtUtil.createAccessToken(userId, userRole);
    }

    @Override
    public String generateRefreshToken(long userId, String userRole) {
        return JwtUtil.createRefreshToken(userId, userRole);
    }

    @Override
    public void saveRefreshToken(String refreshToken, Long userId) {
        redisTemplate.opsForHash().put(REFRESH_KEY, userId.toString(), refreshToken);
    }

    @Override
    public String reissueToken(String refreshToken) {
        Claims claims = Jwts.parser()
                .setSigningKey(Util.SECRET_KEY)
                .parseClaimsJws(refreshToken)
                .getBody();
        Long userId = claims.get("userId", Long.class);
        String role = claims.get("role", String.class);

        String storedRefreshToken = (String) redisTemplate.opsForHash().get(REFRESH_KEY, userId.toString());

        if (!refreshToken.equals(storedRefreshToken)) {
            throw new RefreshTokenNotMatchException("Refresh Token이 일치하지 않습니다.");
        }

        String newAccessToken = JwtUtil.createAccessToken(userId, role);
        return newAccessToken;
    }

}
