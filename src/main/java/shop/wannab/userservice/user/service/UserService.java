package shop.wannab.userservice.user.service;

import shop.wannab.userservice.point.domain.dto.PointUpdateDTO;
import shop.wannab.userservice.user.domain.dto.UserCreateDTO;
import shop.wannab.userservice.user.domain.dto.UserUpdateDTO;
import shop.wannab.userservice.user.domain.entity.User;

public interface UserService {
    User createUser(UserCreateDTO userCreateDTO);

    User readUser(long userId);

    User updateUser(long userId, UserUpdateDTO userupdateDTO);

    User login(String username, String password);

    String generateAccessToken(long userId, String userRole);

    String generateRefreshToken(long userId, String userRole);

    void saveRefreshToken(String refreshToken, Long userId);

    void deleteUser(long userId);

    int readPoint(long userId);

    void updatePoint(long userId, PointUpdateDTO pointUpdateDTO);

    boolean existsUser(long userId);

    String reissueToken(String refreshToken);

    void payco(String authorizationCode);
}
