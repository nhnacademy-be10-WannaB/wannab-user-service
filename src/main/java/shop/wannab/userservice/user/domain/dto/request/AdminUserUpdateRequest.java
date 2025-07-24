package shop.wannab.userservice.user.domain.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminUserUpdateRequest {
    private String nickname;
    private String role;
}
