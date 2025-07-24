package shop.wannab.userservice.user.service;

import java.util.List;
import org.springframework.data.domain.Page;
import shop.wannab.userservice.auth.dto.response.ReissueResponse;
import shop.wannab.userservice.auth.dto.response.UserResponse;
import shop.wannab.userservice.point.domain.dto.request.PointUpdateDTO;
import shop.wannab.userservice.user.domain.dto.request.AdminUserUpdateRequest;
import shop.wannab.userservice.user.domain.dto.request.UserCreateRequest;
import shop.wannab.userservice.user.domain.dto.request.UserUpdateRequest;
import shop.wannab.userservice.user.domain.dto.response.AdminPageUserResponse;
import shop.wannab.userservice.user.domain.dto.response.UserPageResponse;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.domain.entity.UserGrade;

public interface UserService {
    User createUser(UserCreateRequest userCreateDTO);

    User readUser(long userId);

    UserPageResponse updateUser(long userId, UserUpdateRequest userUpdateDTO);

    UserPageResponse readUserPageResponse(long userId);

    void deleteUser(long userId);

    int readPoint(long userId);

    void updatePoint(long userId, PointUpdateDTO pointUpdateDTO);

    boolean existsUser(long userId);

    ReissueResponse reissueToken(String refreshToken);

    void logout(long userId);

    List<Long> birthUserList(int month);

    UserResponse readUserResponse(String username);

    boolean duplicated(String username);

    UserGrade getStandardUserGrade();

    Page<AdminPageUserResponse> readUserList(int page, int size);

    AdminPageUserResponse readAdminPageUser(String loginId);

    void updateAdminUser(String loginId, AdminUserUpdateRequest adminUserUpdateRequest);

    void deleteAdminUser(String loginId);
}
