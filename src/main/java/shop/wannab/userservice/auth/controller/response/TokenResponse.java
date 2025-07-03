package shop.wannab.userservice.auth.controller.response;

public record TokenResponse(String accessToken, String refreshToken) {
}
