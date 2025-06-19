package shop.wannab.userservice.user.exception;

public class UsernameOrPasswordMismatchException extends RuntimeException {
    public UsernameOrPasswordMismatchException(String message) {
        super(message);
    }
}
