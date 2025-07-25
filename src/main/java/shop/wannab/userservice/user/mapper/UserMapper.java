package shop.wannab.userservice.user.mapper;

import org.springframework.stereotype.Component;
import shop.wannab.userservice.user.domain.dto.request.UserCreateRequest;
import shop.wannab.userservice.user.domain.dto.response.UserPageResponse;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.domain.entity.UserGrade;

@Component
public class UserMapper {


    public static User userCreateDtoToUser(UserCreateRequest userCreateDTO, UserGrade userGrade) {
        return User.standard()
                .password(userCreateDTO.password())
                .userLoginId(userCreateDTO.userLoginId())
                .name(userCreateDTO.name())
                .email(userCreateDTO.email())
                .phone(userCreateDTO.phone())
                .birth(userCreateDTO.birth())
                .userGrade(userGrade)
                .build();
    }

    public static UserPageResponse UserToUserPageResponse(User user) {
        return UserPageResponse.builder()
                .username(user.getUserLoginId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birth(user.getBirth())
                .nickname(user.getNickname())
                .password(user.getPassword())
                .points(user.getPoints())
                .build();
    }
}
