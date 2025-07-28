package shop.wannab.userservice.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.userservice.auth.dto.request.PaycoLoginRequest;
import shop.wannab.userservice.auth.dto.request.ReissueRequest;
import shop.wannab.userservice.auth.dto.request.TokenPayloadRequest;
import shop.wannab.userservice.auth.dto.request.TokenRequest;
import shop.wannab.userservice.auth.dto.request.UnlockRequest;
import shop.wannab.userservice.auth.dto.response.PaycoLoginResponse;
import shop.wannab.userservice.auth.dto.response.ReissueResponse;
import shop.wannab.userservice.auth.dto.response.TokenPayloadResponse;
import shop.wannab.userservice.auth.dto.response.TokenResponse;
import shop.wannab.userservice.auth.dto.response.UserResponse;
import shop.wannab.userservice.auth.service.AuthService;
import shop.wannab.userservice.global.Response;
import shop.wannab.userservice.point.service.PointHistoryService;
import shop.wannab.userservice.user.domain.dto.request.UserCreateRequest;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.service.UserService;
import shop.wannab.userservice.utils.ResponseCode;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final PointHistoryService pointHistoryService;


    @PostMapping("/reissue")
    public ResponseEntity<ReissueResponse> refreshAccessToken(@RequestBody ReissueRequest reissueRequest) {

        ReissueResponse reissueResponse = userService.reissueToken(reissueRequest.refreshToken());

        log.info("action=refreshAccessToken, message=\"Access Token 재발급 완료\"");

        return ResponseEntity.ok(reissueResponse);
    }

    @PostMapping("/signup")
    public Response<Void> createUser(@RequestBody @Valid UserCreateRequest userCreateDTO) {
        User user = userService.createUser(userCreateDTO);
        pointHistoryService.createSignupPoints(user);

        log.info("action=createUser, userId={}, userLoginId=\"{}\", message=\"사용자 회원가입 완료 및 포인트 적립\"", user.getUserId(),
                user.getUserLoginId());

        return new Response<>(null, ResponseCode.SUCCESS, null);
    }

    @PostMapping("/token")
    public ResponseEntity<TokenResponse> token(@RequestBody TokenRequest tokenRequest) {

        log.info("action=token, userId=\"{}\", message=\"로그인 토큰 발급 완료\"", tokenRequest.userId());

        return ResponseEntity.ok(authService.login(tokenRequest));
    }

    @PostMapping("/login/payco")
    public ResponseEntity<Response<PaycoLoginResponse>> paycoLogin(@RequestBody PaycoLoginRequest paycoLoginRequest) {
        Response<PaycoLoginResponse> response = authService.paycoLogin(paycoLoginRequest);

        log.info("action=paycoLogin, providerId=\"{}\", message=\"페이코 로그인 완료\"", paycoLoginRequest.providerId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public UserResponse login(@RequestParam String loginId) {

        log.info("action=login, loginId=\"{}\", message=\"사용자 정보 조회 완료 (로그인 목적)\"", loginId);

        return userService.readUserResponse(loginId);
    }

    @PostMapping("/unlock/request")
    public ResponseEntity<String> unlock(@RequestBody String userId) {
        authService.unlockRequest(userId);

        log.info("action=unlockRequest, userId=\"{}\", message=\"계정 잠금 해제 요청 완료\"", userId);

        return ResponseEntity.ok().body(userId);
    }

    @PostMapping("/unlock/verify")
    public ResponseEntity<Boolean> unlock(@RequestBody UnlockRequest request) {
        boolean result = authService.unlock(request);

        log.info("action=unlockVerify, userId=\"{}\", result={}, message=\"계정 잠금 해제 검증 완료\"", request.userId(), result);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/duplicated")
    public Response<Boolean> duplicated(@RequestParam("id") String userId) {
        Boolean duplicated = userService.duplicated(userId);

        log.info("action=duplicated, checkId=\"{}\", isDuplicated={}, message=\"사용자 ID 중복 확인 완료\"", userId, duplicated);

        return new Response<>(duplicated, ResponseCode.SUCCESS, null);
    }

    @PostMapping("/info")
    public ResponseEntity<TokenPayloadResponse> info(@RequestBody @Valid TokenPayloadRequest tokenPayloadRequest) {
        TokenPayloadResponse response = authService.getTokenPayload(tokenPayloadRequest);

        log.info("action=info,token={}, message=\"토큰 페이로드 정보 조회 완료\"", tokenPayloadRequest.token());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/lastLogin")
    public ResponseEntity<Void> updateLastLogin(@RequestParam("userId") Long userId) {
        authService.updateLastLogin(userId);

        log.info("action=updateLastLogin, userId={}, message=\"최근 로그인 시간 업데이트 완료\"", userId);
        return ResponseEntity.noContent().build();
    }

}
