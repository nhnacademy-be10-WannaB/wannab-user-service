package shop.wannab.userservice.point.exception;

public class PointPolicyAlreadyExistsException extends RuntimeException {
    public PointPolicyAlreadyExistsException(String message) {
        super(message);
    }

    public PointPolicyAlreadyExistsException() {
        super("같은 이름의 포인트 정책 있음");
    }

}
