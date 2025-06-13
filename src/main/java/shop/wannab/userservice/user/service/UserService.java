package shop.wannab.userservice.user.service;

import shop.wannab.userservice.user.domain.dto.UserCreateDTO;
import shop.wannab.userservice.user.domain.entity.User;

public interface UserService {
    void createUser(UserCreateDTO userCreateDTO);
}
