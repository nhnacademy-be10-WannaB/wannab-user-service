package shop.wannab.userservice.user.service;

import shop.wannab.userservice.user.domain.dto.UserCreateDTO;
import shop.wannab.userservice.user.domain.dto.UserUpdateDTO;
import shop.wannab.userservice.user.domain.entity.User;

public interface UserService {
    User createUser(UserCreateDTO userCreateDTO);

    User readUser(long userId);

    User updateUser(long userId, UserUpdateDTO userupdateDTO);

    String login(String username, String password);

    void deleteUser(long userId);
}
