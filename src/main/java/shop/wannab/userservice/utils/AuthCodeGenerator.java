package shop.wannab.userservice.utils;

import java.security.SecureRandom;

public class AuthCodeGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();

    public int generate6DigitCode() {
        return 100000 + secureRandom.nextInt(900000);
    }
}