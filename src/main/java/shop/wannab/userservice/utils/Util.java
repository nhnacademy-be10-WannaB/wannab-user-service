package shop.wannab.userservice.utils;


import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import org.springframework.beans.factory.annotation.Value;

public class Util {
    public final static String HEADER_ID_NAME = "X-USER-ID";
    public final static String HEADER_ROLE_NAME = "X-USER-ROLE";

    @Value("${jwt.secret-key}")
    public static String SECRET;
    public static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

}
