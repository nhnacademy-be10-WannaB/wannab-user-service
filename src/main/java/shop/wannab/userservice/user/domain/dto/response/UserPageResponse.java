package shop.wannab.userservice.user.domain.dto.response;

import java.time.LocalDate;
import lombok.Builder;
import shop.wannab.userservice.user.domain.entity.UserGrade;

@Builder
public record UserPageResponse(String name,
                               String nickname,
                               String email,
                               String phone,
                               LocalDate birth,
                               String username,
                               String password,
                               int points,
                               UserGrade userGrade
) {
}
