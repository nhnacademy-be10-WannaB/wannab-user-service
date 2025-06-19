package shop.wannab.userservice.user.domain.dto;

public record LoginResponse(String accessToken, String refreshToken) {
}
