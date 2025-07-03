package shop.wannab.userservice.auth.controller.response;

import lombok.Builder;
import shop.wannab.userservice.user.domain.entity.Role;
import shop.wannab.userservice.user.domain.entity.State;

public record LoginResponse(Long userId,
                            String password,
                            String loginId,
                            Role role,
                            State state
) {

    @Builder
    public LoginResponse {
    }
}
