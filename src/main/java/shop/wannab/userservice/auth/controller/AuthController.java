package shop.wannab.userservice.auth.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.userservice.user.service.UserService;
import shop.wannab.userservice.utils.Util;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

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

    @GetMapping("/payco-register")
    public ResponseEntity paycoRegeister(@RequestParam(name = "authorization_code") String authorizationCode) {
        userService.payco(authorizationCode);
        return ResponseEntity.ok().build();
    }

}
