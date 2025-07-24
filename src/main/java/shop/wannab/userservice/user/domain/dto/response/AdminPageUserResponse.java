package shop.wannab.userservice.user.domain.dto.response;

import java.time.LocalDate;
import lombok.Data;
import shop.wannab.userservice.user.domain.entity.User;

@Data
public class AdminPageUserResponse {
    private String name;
    private String nickname;
    private String email;
    private String phone;
    private LocalDate birth;
    private String username;
    private String role;

    public AdminPageUserResponse(User user) {
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.birth = user.getBirth();
        this.username = user.getUserLoginId();
        this.role = user.getRole().name();
    }
}