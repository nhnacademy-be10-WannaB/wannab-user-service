package shop.wannab.userservice.user.controller;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.userservice.user.domain.dto.CommonResponse;
import shop.wannab.userservice.user.domain.dto.UserLoginDTO;
import shop.wannab.userservice.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<String>> login(@RequestBody UserLoginDTO userLoginDTO) {
        CommonResponse<String> response = new CommonResponse<>();
        String jwt = userService.login(userLoginDTO.getUsername(), userLoginDTO.getPassword());
        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ofMinutes(30))
                .build();
        response.setData(jwt);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
