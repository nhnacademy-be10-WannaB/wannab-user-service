package shop.wannab.userservice.auth.controller.request;

public record TokenRequest(Long userId, String role) {
}
