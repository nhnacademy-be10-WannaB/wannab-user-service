package shop.wannab.userservice.auth.controller;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.userservice.auth.controller.request.TokenRequest;
import shop.wannab.userservice.auth.controller.response.TokenResponse;
import shop.wannab.userservice.auth.service.AuthService;
import shop.wannab.userservice.auth.controller.response.UserResponse;
import shop.wannab.userservice.user.domain.dto.request.UserCreateRequest;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.service.UserService;
import shop.wannab.userservice.utils.JwtUtil;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @GetMapping("/refresh-token")
    public ResponseEntity refreshAccessToken(@RequestHeader("X-REFRESH-TOKEN") String refreshToken) {

        String newAccessToken = userService.reissueToken(refreshToken);

        return ResponseEntity.ok(newAccessToken);
    }

    @PostMapping("/signup")
    public ResponseEntity<User> createUser(@RequestBody @Valid UserCreateRequest userCreateDTO) {
        User user = userService.createUser(userCreateDTO);
        URI uri = URI.create("/api/users/" + user.getUserId());
        return ResponseEntity.created(uri).body(user);
    }

    @PostMapping("/token")
    public ResponseEntity<TokenResponse> token(@RequestBody TokenRequest tokenRequest) {
        return ResponseEntity.ok(authService.login(tokenRequest));
    }

    @GetMapping("/users")
    public UserResponse login(@RequestBody String loginId) {
        UserResponse userResponse = userService.findByUsername(loginId);
        return userResponse;
    }

}
