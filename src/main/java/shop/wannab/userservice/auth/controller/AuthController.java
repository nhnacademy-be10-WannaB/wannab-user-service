package shop.wannab.userservice.auth.controller;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.userservice.auth.controller.request.PaycoLoginRequest;
import shop.wannab.userservice.auth.controller.request.TokenRequest;
import shop.wannab.userservice.auth.controller.response.PaycoLoginResponse;
import shop.wannab.userservice.auth.controller.request.ReissueRequest;
import shop.wannab.userservice.auth.controller.response.ReissueResponse;
import shop.wannab.userservice.auth.controller.response.TokenResponse;
import shop.wannab.userservice.auth.controller.response.UserResponse;
import shop.wannab.userservice.auth.service.AuthService;
import shop.wannab.userservice.global.Response;
import shop.wannab.userservice.user.domain.dto.request.UserCreateRequest;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;


    @PostMapping("/reissue")
    public ResponseEntity refreshAccessToken(@RequestBody ReissueRequest reissueRequest) {

        String newAccessToken = userService.reissueToken(reissueRequest.refreshToken());

        return ResponseEntity.ok(new ReissueResponse(newAccessToken));
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
  
    @PostMapping("/login/payco")
    public ResponseEntity<Response<PaycoLoginResponse>> paycoLogin(@RequestBody PaycoLoginRequest paycoLoginRequest) {
        Response<PaycoLoginResponse> response = authService.paycoLogin(paycoLoginRequest);
        return ResponseEntity.ok(response);
    }
  
    @GetMapping("/users")
    public UserResponse login(@RequestParam String loginId) {
        UserResponse userResponse = userService.findByUsername(loginId);
        return userResponse;
    }

}
