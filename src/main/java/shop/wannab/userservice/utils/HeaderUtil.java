package shop.wannab.userservice.utils;

public class HeaderUtil {
    private HeaderUtil() {
        throw new UnsupportedOperationException("객체로 생성 될 수 없습니다.");
    }

    public static final String HEADER_ID_NAME = "X-USER-ID";
    public static final String HEADER_ROLE_NAME = "X-USER-ROLE";
}
