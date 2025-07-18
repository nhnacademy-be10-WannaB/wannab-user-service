package shop.wannab.userservice.auth.dto.request;

public record TokenRequest(Long userId, String role) {
}
