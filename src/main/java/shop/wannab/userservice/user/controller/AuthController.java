package shop.wannab.userservice.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.userservice.auth.controller.request.LoginRequest;
import shop.wannab.userservice.auth.controller.response.LoginResponse;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid LoginRequest request) {
        User user = userService.login(request.username(), request.password());

        String accessToken = userService.generateAccessToken(user.getUserId(), user.getRole().name());
        String refreshToken = userService.generateRefreshToken(user.getUserId(), user.getRole().name());

        userService.saveRefreshToken(refreshToken, user.getUserId());

        return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken));
    }

    @GetMapping("/refresh-token")
    public ResponseEntity refreshAccessToken(@RequestHeader("X-REFRESH-TOKEN") String refreshToken) {

        String newAccessToken = userService.reissueToken(refreshToken);

        return ResponseEntity.ok(newAccessToken);
    }

}
