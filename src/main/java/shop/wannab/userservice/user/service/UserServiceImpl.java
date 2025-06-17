package shop.wannab.userservice.user.service;

import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.userservice.user.domain.dto.UserCreateDTO;
import shop.wannab.userservice.user.domain.dto.UserUpdateDTO;
import shop.wannab.userservice.user.domain.entity.Role;
import shop.wannab.userservice.user.domain.entity.State;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.exception.UserAlreadyExistsException;
import shop.wannab.userservice.user.exception.UserNotFoundException;
import shop.wannab.userservice.user.repository.UserRepository;
import shop.wannab.userservice.util.JwtUtil;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REFRESH_KEY = "refresh_token:";

    public void createUser(UserCreateDTO userCreateDTO) {
        if (userRepository.existsByUsername(userCreateDTO.username())) {
            throw new UserAlreadyExistsException();
        }

        User user = new User(
                userCreateDTO.password(),
                userCreateDTO.username(),
                userCreateDTO.name(),
                userCreateDTO.email(),
                "user",
                userCreateDTO.phone(),
                userCreateDTO.birth(),
                LocalDate.now(),
                0,
                Role.USER,
                State.ACTIVATE,
                null,
                null);
        userRepository.save(user);
    }

    @Override
    public User readUser(long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            throw new UserNotFoundException();
        }
    }

    @Override
    public void updateUser(UserUpdateDTO userupdateDTO) {

    }

    @Override
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
            return user;
        } else {
            throw new UserNotFoundException();
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


}
