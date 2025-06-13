package shop.wannab.userservice.user.exception;

public class UserIdMismatchException extends RuntimeException {
    public UserIdMismatchException() {
        super("요청한 사용자와 대상 사용자가 일치하지 않음");
    }
}