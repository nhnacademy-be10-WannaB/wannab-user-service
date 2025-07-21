package shop.wannab.userservice.user.domain.dto.request;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import shop.wannab.userservice.user.domain.entity.UserGrade;

@Builder
@Getter
public class UserCreateDTO {
    String password;
    String userLoginId;
    String name;
    String email;
    String phone;
    LocalDate birth;
    String providerName;
    String providerId;
    UserGrade userGrade;
}
