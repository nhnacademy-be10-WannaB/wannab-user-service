package shop.wannab.userservice.point.exception;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException() {
        super("정책 변경은 관리자 권한 필요");
    }
}
