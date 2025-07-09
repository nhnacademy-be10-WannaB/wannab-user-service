package shop.wannab.userservice.auth.controller;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.userservice.auth.controller.request.PaycoLoginRequest;
import shop.wannab.userservice.auth.controller.request.ReissueRequest;
import shop.wannab.userservice.auth.controller.request.TokenRequest;
import shop.wannab.userservice.auth.controller.request.UnlockRequest;
import shop.wannab.userservice.auth.controller.response.PaycoLoginResponse;
import shop.wannab.userservice.auth.controller.response.ReissueResponse;
import shop.wannab.userservice.auth.controller.response.TokenResponse;
import shop.wannab.userservice.auth.controller.response.UserResponse;
import shop.wannab.userservice.auth.service.AuthService;
import shop.wannab.userservice.global.Response;
import shop.wannab.userservice.user.domain.dto.request.UserCreateRequest;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.service.UserService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;


    @PostMapping("/reissue")
    public ResponseEntity refreshAccessToken(@RequestBody ReissueRequest reissueRequest) {
        log.info("Controller: refreshAccessToken");

        String newAccessToken = userService.reissueToken(reissueRequest.refreshToken());

        return ResponseEntity.ok(new ReissueResponse(newAccessToken));
    }

    @PostMapping("/signup")
    public ResponseEntity<User> createUser(@RequestBody @Valid UserCreateRequest userCreateDTO) {
        log.info("Controller: createUser");
        User user = userService.createUser(userCreateDTO);
        URI uri = URI.create("/api/users/" + user.getUserId());
        return ResponseEntity.created(uri).body(user);
    }

    @PostMapping("/token")
    public ResponseEntity<TokenResponse> token(@RequestBody TokenRequest tokenRequest) {
        log.info("Controller: token");
        return ResponseEntity.ok(authService.login(tokenRequest));
    }

    @PostMapping("/login/payco")
    public ResponseEntity<Response<PaycoLoginResponse>> paycoLogin(@RequestBody PaycoLoginRequest paycoLoginRequest) {
        log.info("Controller: paycoLogin");
        Response<PaycoLoginResponse> response = authService.paycoLogin(paycoLoginRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public UserResponse login(@RequestParam String loginId) {
        log.info("Controller: login");
        UserResponse userResponse = userService.findByUsername(loginId);
        return userResponse;
    }

    @PostMapping("/unlock/request")
    public ResponseEntity unlock(@RequestBody String userId) {
        authService.unlockRequest(userId);
        return ResponseEntity.ok().body(userId);
    }

    @PostMapping("/unlock/verify")
    public ResponseEntity unlock(@RequestBody UnlockRequest request) {
        boolean result = authService.unlock(request);
        return ResponseEntity.ok(result);
    }
}
