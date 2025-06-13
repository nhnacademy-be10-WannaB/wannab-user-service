package shop.wannab.userservice.user.exception;

public class AddressLimitExceededException extends RuntimeException {

    public AddressLimitExceededException() {
        super("주소를 더이상 등록할 수 없음");
    }

    public AddressLimitExceededException(String message) {
        super(message);
    }
}