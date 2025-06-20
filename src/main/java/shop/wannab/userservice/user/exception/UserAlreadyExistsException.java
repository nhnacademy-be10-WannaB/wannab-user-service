package shop.wannab.userservice.user.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException() {
        super("이미 존재하는 유저임");
    }

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
