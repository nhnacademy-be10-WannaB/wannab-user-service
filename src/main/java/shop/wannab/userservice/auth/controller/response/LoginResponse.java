package shop.wannab.userservice.auth.controller.response;

public record LoginResponse(String accessToken, String refreshToken) {
}
