package shop.wannab.userservice.auth.dto.response;

import lombok.Builder;
import shop.wannab.userservice.user.domain.entity.Role;
import shop.wannab.userservice.user.domain.entity.State;

@Builder
public record UserResponse(Long userId,
                           String password,
                           String loginId,
                           State state,
                           Role role
) {
}
