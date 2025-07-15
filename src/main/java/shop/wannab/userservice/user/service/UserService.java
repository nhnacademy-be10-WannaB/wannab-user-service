package shop.wannab.userservice.user.service;

import java.util.List;
import shop.wannab.userservice.auth.controller.response.UserResponse;
import shop.wannab.userservice.point.domain.dto.PointUpdateDTO;
import shop.wannab.userservice.user.domain.dto.request.UserCreateRequest;
import shop.wannab.userservice.user.domain.dto.request.UserUpdateRequest;
import shop.wannab.userservice.user.domain.dto.response.UserPageResponse;
import shop.wannab.userservice.user.domain.entity.User;

public interface UserService {
    User createUser(UserCreateRequest userCreateDTO);

    User readUser(long userId);

    UserPageResponse readUserPageResponse(long userId);

    User updateUser(long userId, UserUpdateRequest userupdateDTO);

    void saveRefreshToken(String refreshToken, Long userId);

    void deleteUser(long userId);

    int readPoint(long userId);

    void updatePoint(long userId, PointUpdateDTO pointUpdateDTO);

    boolean existsUser(long userId);

    String reissueToken(String refreshToken);

    void logout(long userId);

    List<Long> birthUserList(int month);

    UserResponse findByUsername(String username);

    boolean duplicated(String username);

    //TODO 배포전 삭제
    User human(Long userId);
}
