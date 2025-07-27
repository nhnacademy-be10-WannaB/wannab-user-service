package shop.wannab.userservice.auth.service;

import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import shop.wannab.userservice.auth.DoorayMessageClient;
import shop.wannab.userservice.auth.dto.request.PaycoLoginRequest;
import shop.wannab.userservice.auth.dto.request.SendMessageRequest;
import shop.wannab.userservice.auth.dto.request.TokenPayloadRequest;
import shop.wannab.userservice.auth.dto.request.TokenRequest;
import shop.wannab.userservice.auth.dto.request.UnlockRequest;
import shop.wannab.userservice.auth.dto.response.PaycoLoginResponse;
import shop.wannab.userservice.auth.dto.response.TokenPayloadResponse;
import shop.wannab.userservice.auth.dto.response.TokenResponse;
import shop.wannab.userservice.global.Response;
import shop.wannab.userservice.user.domain.entity.State;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.domain.entity.UserGrade;
import shop.wannab.userservice.user.exception.UserNotFoundException;
import shop.wannab.userservice.user.repository.UserRepository;
import shop.wannab.userservice.user.service.UserService;
import shop.wannab.userservice.utils.AuthCodeGenerator;
import shop.wannab.userservice.utils.JwtUtil;
import shop.wannab.userservice.utils.ResponseCode;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    private final DoorayMessageClient doorayMessageClient;
    private static final String REFRESH_KEY = "refresh_token:";
    private final UserService userService;

    public TokenResponse login(TokenRequest tokenRequest) {
        log.info("action=login, userId={}, role={}, message=\"로그인 서비스 시작\"", tokenRequest.userId(), tokenRequest.role());

        String accessToken = jwtUtil.createAccessToken(tokenRequest.userId(), tokenRequest.role());
        String refreshToken = jwtUtil.createRefreshToken(tokenRequest.userId(), tokenRequest.role());
        redisTemplate.opsForHash().put(REFRESH_KEY, String.valueOf(tokenRequest.userId()), refreshToken);
        log.debug("action=login, userId={}, message=\"Redis에 리프레시 토큰 저장 완료\"", tokenRequest.userId());

        log.info("action=login, userId={}, message=\"로그인 서비스 완료: 토큰 발급\"", tokenRequest.userId());
        return new TokenResponse(accessToken, refreshToken);
    }

    public Response<PaycoLoginResponse> paycoLogin(PaycoLoginRequest paycoLoginRequest) {
        log.info("action=paycoLogin, providerId={}, message=\"페이코 로그인/회원가입 서비스 시작\"", paycoLoginRequest.providerId());

        if (userRepository.existsByProviderId(paycoLoginRequest.providerId())) {
            User user = userRepository.findByProviderId(paycoLoginRequest.providerId()).get();
            log.debug("action=paycoLogin, providerId={}, userId={}, message=\"기존 페이코 사용자 발견\"", paycoLoginRequest.providerId(), user.getUserId());

            String token = (String) redisTemplate.opsForHash().get(REFRESH_KEY, "1");
            log.info("token: {}", token);
            return new Response<>(new PaycoLoginResponse(user.getUserId(), user.getRole().toString()),
                    ResponseCode.PAYCO_LOGIN_SUCESS, "로그인 성공 및 토큰 반환");
        } else {
            User user = userRepository.save(buildUserByPaycoLoginRequest(paycoLoginRequest, userService.getStandardUserGrade()));
            return new Response<>(new PaycoLoginResponse(user.getUserId(), user.getRole().toString()),
                    ResponseCode.PAYCO_SIGNUP_SUCESS, "회원가입 성공");
        }
    }

    public User buildUserByPaycoLoginRequest(PaycoLoginRequest paycoLoginRequest, UserGrade userGrade) {
        log.info("action=buildUserByPaycoLoginRequest, providerId={}, name=\"{}\", message=\"페이코 로그인 정보 기반 사용자 객체 생성 시작\"", paycoLoginRequest.providerId(), paycoLoginRequest.name());

        log.info("action=buildUserByPaycoLoginRequest, providerId={}, name=\"{}\", message=\"사용자 객체 생성 완료\"", paycoLoginRequest.providerId(), paycoLoginRequest.name());

        return new User(
                paycoLoginRequest.providerId(),
                paycoLoginRequest.providerName(),
                paycoLoginRequest.name(),
                paycoLoginRequest.email(),
                paycoLoginRequest.birthday(),
                userGrade,
                paycoLoginRequest.phone()
                );
    }

    public void unlockRequest(String userId) {
        log.info("action=unlockRequest, userId=\"{}\", message=\"계정 잠금 해제 코드 요청 서비스 시작\"", userId);

        AuthCodeGenerator authCodeGenerator = new AuthCodeGenerator();
        int code = authCodeGenerator.generate6DigitCode();
        redisTemplate.opsForValue().set("UNLOCK_CODE:" + userId, code, 3, TimeUnit.MINUTES);
        log.debug("action=unlockRequest, userId=\"{}\", code={}, expiresInMinutes=3, message=\"Redis에 잠금 해제 코드 저장 완료\"", userId, code);

        doorayMessageClient.sendUnlockCode(SendMessageRequest.unlockCodeMessage(userId, code));
        log.info("action=unlockRequest, userId=\"{}\", message=\"계정 잠금 해제 코드 요청 서비스 완료\"", userId);

    }

    public boolean unlock(UnlockRequest request) {
        log.info("action=unlock, userId=\"{}\", message=\"계정 잠금 해제 검증 서비스 시작\"", request.userId());

        String key = "UNLOCK_CODE:" + request.userId();
        Object savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null || !(savedCode.toString().equals(String.valueOf(request.authenticationCode())))) {
            return false;
        }
        if (!userRepository.existsByUserLoginId(request.userId())) {
            throw new UserNotFoundException(request.userId());
        }
        User user = userRepository.findByUserLoginId(request.userId()).get();
        user.setState(State.ACTIVATE);

        redisTemplate.delete(key);
        log.info("action=unlock, userId=\"{}\", message=\"계정 잠금 해제 검증 서비스 완료: 성공\"", request.userId());

        return true;
    }

    public TokenPayloadResponse getTokenPayload(TokenPayloadRequest tokenPayloadRequest) {
        log.info("action=getTokenPayload, message=\"토큰 페이로드 추출 서비스 시작\"");

        Claims claims = jwtUtil.parseToken(tokenPayloadRequest.token());
        log.info("action=getTokenPayload, userIdFromToken={}, roleFromToken={}, message=\"토큰 페이로드 추출 서비스 완료\"",
                claims.getSubject(), claims.get("role"));

        return new TokenPayloadResponse(claims);
    }

    public void updateLastLogin(Long userId) {
        log.info("action=updateLastLogin, userId={}, message=\"최근 로그인 시간 업데이트 서비스 시작\"", userId);

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.setLastLoginAt(LocalDate.now());
        log.info("action=updateLastLogin, userId={}, newLastLoginAt={}, message=\"최근 로그인 시간 업데이트 서비스 완료\"", userId, user.getLastLoginAt());

    }
}
