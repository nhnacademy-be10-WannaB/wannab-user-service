package shop.wannab.userservice.auth.dto.request;

public record UnlockRequest(
        String userId,
        int authenticationCode
) {
}
