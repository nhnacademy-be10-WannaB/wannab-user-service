package shop.wannab.userservice.point.exception;

public class PointPolicyNotFoundException extends RuntimeException {
    public PointPolicyNotFoundException(String message) {
        super(message);
    }

    public PointPolicyNotFoundException() {
        super("포인트 정책 찾을 수 없음");
    }
}
