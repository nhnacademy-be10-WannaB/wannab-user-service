package shop.wannab.userservice.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.userservice.user.domain.dto.UserCreateDTO;
import shop.wannab.userservice.user.domain.entity.Role;
import shop.wannab.userservice.user.domain.entity.State;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.exception.UserAlreadyExistsException;
import shop.wannab.userservice.user.repository.UserRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public void createUser(UserCreateDTO userCreateDTO) {
        if(userRepository.existsByUsername(userCreateDTO.username())){
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


}
