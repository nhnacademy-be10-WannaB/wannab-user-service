package shop.wannab.userservice.address.exception;

public class UserAddressNotFoundException extends RuntimeException {

    public UserAddressNotFoundException() {
        super("해당 사용자의 주소 정보를 찾을 수 없습니다.");
    }

    public UserAddressNotFoundException(String message) {
        super(message);
    }
}
