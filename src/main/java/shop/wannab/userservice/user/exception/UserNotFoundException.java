package shop.wannab.userservice.user.exception;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException() {super("해당하는 사용자 없음");}
  public UserNotFoundException(String message) {
        super(message);
    }

}
