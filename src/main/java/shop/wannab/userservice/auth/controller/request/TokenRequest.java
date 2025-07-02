package shop.wannab.userservice.auth.controller.request;

import shop.wannab.userservice.user.domain.entity.Role;

public record TokenRequest(Long userId, Role role) {
}
