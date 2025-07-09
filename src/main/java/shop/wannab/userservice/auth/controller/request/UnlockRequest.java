package shop.wannab.userservice.auth.controller.request;

public record UnlockRequest(
        String userId,
        int authenticationCode
) {
}
