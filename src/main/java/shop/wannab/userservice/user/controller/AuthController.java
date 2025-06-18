package shop.wannab.userservice.user.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.userservice.user.domain.dto.UserLoginDTO;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.service.UserService;
import shop.wannab.userservice.utils.Util;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid UserLoginDTO userLoginDTO) {
        User user = userService.login(userLoginDTO.getUsername(), userLoginDTO.getPassword());

        String accessToken = userService.generateAccessToken(user.getUserId(), user.getRole().name());
        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", accessToken)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMinutes(30))
                .build();

        String refreshToken = userService.generateRefreshToken(user.getUserId(), user.getRole().name());
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        userService.saveRefreshToken(refreshToken, user.getUserId());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity refreshAccessToken(@CookieValue("refresh_token") String refreshToken) {
        Claims claims = Jwts.parser()
                .setSigningKey(Util.SECRET)
                .parseClaimsJws(refreshToken)
                .getBody();
        Long userId = claims.get("userId", Long.class);
        String role = claims.get("role", String.class);

        String newAccessToken = userService.generateAccessToken(userId, role);

        return ResponseEntity.ok(newAccessToken);
    }

}
