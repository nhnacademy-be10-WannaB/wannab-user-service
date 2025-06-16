package shop.wannab.userservice.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.userservice.user.domain.dto.UserCreateDTO;
import shop.wannab.userservice.user.domain.dto.UserUpdateDTO;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.exception.UserAlreadyExistsException;
import shop.wannab.userservice.user.exception.UserNotFoundException;
import shop.wannab.userservice.user.repository.UserRepository;
import shop.wannab.userservice.utils.JwtUtil;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public void createUser(UserCreateDTO userCreateDTO) {
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
        userRepository.save(user);
    }

    @Override
    public User readUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 유저 없음"));
    }

    @Override
    public void updateUser(long userId, UserUpdateDTO userupdateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 유저 없음"));
        user.setName(userupdateDTO.name());
        user.setEmail(userupdateDTO.email());
        user.setPhone(userupdateDTO.phone());
        user.setNickname(userupdateDTO.nickname());
        user.setPassword(userupdateDTO.password());
    }

    @Override
    public void deleteUser(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("해당하는 유저 없음");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public String login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
            return JwtUtil.createAccessToken(user.getUserId(), user.getRole().name());
        } else {
            throw new UserNotFoundException();
        }
    }


}
