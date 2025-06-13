package shop.wannab.userservice.util;


import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;

public class Util {
    public final static String HEADER_ID_NAME = "X-USER-ID";
    public static final String SECRET = "P3C+yJU3dv7iXM2umvApxy7NTkfm2BL6V3GBIPTbe/Q=";
    public static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

}
