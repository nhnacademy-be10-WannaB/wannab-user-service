package shop.wannab.userservice.utils;


import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import org.springframework.beans.factory.annotation.Value;

public class Util {
    public final static String HEADER_ID_NAME = "X-USER-ID";
    public final static String HEADER_ROLE_NAME = "X-USER-ROLE";

    public static String SECRET;
    public static Key SECRET_KEY;

    @Value("${jwt.secret-key}")
    public String secretValue;

    @PostConstruct
    public void init() {
        SECRET = secretValue;
        SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }
}
