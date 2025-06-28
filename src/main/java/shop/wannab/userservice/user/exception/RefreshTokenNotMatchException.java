package shop.wannab.userservice.user.exception;

public class RefreshTokenNotMatchException extends RuntimeException {
    public RefreshTokenNotMatchException(String message) {
        super(message);
    }
}
