package shop.wannab.userservice.user.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.userservice.user.domain.dto.LoginResponse;
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
        String refreshToken = userService.generateRefreshToken(user.getUserId(), user.getRole().name());

        userService.saveRefreshToken(refreshToken, user.getUserId());

        return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity refreshAccessToken(@RequestHeader("X-REFRESH-TOKEN") String refreshToken) {
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
