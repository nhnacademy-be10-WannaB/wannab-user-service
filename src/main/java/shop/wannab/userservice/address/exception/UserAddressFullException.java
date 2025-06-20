package shop.wannab.userservice.address.exception;

public class UserAddressFullException extends RuntimeException {
    public UserAddressFullException() {
        super("해당 사용자의 주소가 10개입니다.");
    }

    public UserAddressFullException(String message) {
        super(message);
    }
}
