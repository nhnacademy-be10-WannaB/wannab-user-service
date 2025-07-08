package shop.wannab.userservice.utils;

public final class ResponseCode {

    private ResponseCode() {
    }

    // 00 success
    public static final String SUCCESS = "00000";

    // 01 address
    public static final String ADDRESS_IS_FULL = "01001";
    public static final String ADDRESS_NOT_FOUND = "01002";
    public static final String ADDRESS_ALREADY_EXISTS = "01003";


    // 02 auth
    public static final String PAYCO_SIGNUP_SUCESS = "02001";
    public static final String PAYCO_LOGIN_SUCESS = "02002";

    // 03 point

    // 04 user

}
