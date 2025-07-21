package shop.wannab.userservice.auth.service;

import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;
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
import shop.wannab.userservice.user.exception.UserNotFoundException;
import shop.wannab.userservice.user.repository.UserRepository;
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

    public TokenResponse login(TokenRequest tokenRequest) {
        log.info("Service: login");
        String accessToken = jwtUtil.createAccessToken(tokenRequest.userId(), tokenRequest.role());
        String refreshToken = jwtUtil.createRefreshToken(tokenRequest.userId(), tokenRequest.role());
        redisTemplate.opsForHash().put(REFRESH_KEY, String.valueOf(tokenRequest.userId()), refreshToken);
        return new TokenResponse(accessToken, refreshToken);
    }

    public Response<PaycoLoginResponse> paycoLogin(PaycoLoginRequest paycoLoginRequest) {
        log.info("Service: paycoLogin");
        if (userRepository.existsByProviderId(paycoLoginRequest.providerId())) {
            User user = userRepository.findByProviderId(paycoLoginRequest.providerId()).get();
            String token = (String) redisTemplate.opsForHash().get(REFRESH_KEY, "1");
            log.info("token: {}", token);
            return new Response<>(new PaycoLoginResponse(user.getUserId(), user.getRole().toString()),
                    ResponseCode.PAYCO_LOGIN_SUCESS, "로그인 성공 및 토큰 반환");
        } else {
            User user = userRepository.save(buildUserByPaycoLoginRequest(paycoLoginRequest));
            return new Response<>(new PaycoLoginResponse(user.getUserId(), user.getRole().toString()),
                    ResponseCode.PAYCO_SIGNUP_SUCESS, "회원가입 성공");
        }
    }

    public User buildUserByPaycoLoginRequest(PaycoLoginRequest paycoLoginRequest) {
        log.info("Service: buildUserByPaycoLoginRequest");
        String token = (String) redisTemplate.opsForHash().get(REFRESH_KEY, "1");
        log.info("token: {}", token);
        return User.social().providerId(paycoLoginRequest.providerId())
                .email(paycoLoginRequest.email())
                .phone(paycoLoginRequest.phone())
                .birth(paycoLoginRequest.birthday())
                .build();
    }

    public void unlockRequest(String userId) {
        int code = ThreadLocalRandom.current().nextInt(100000, 1_000000);
        redisTemplate.opsForValue().set("UNLOCK_CODE:" + userId, code, 3, TimeUnit.MINUTES);
        doorayMessageClient.sendUnlockCode(SendMessageRequest.unlockCodeMessage(userId, code));
    }

    public boolean unlock(UnlockRequest request) {
        String key = "UNLOCK_CODE:" + request.userId();
        Object savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null || !(savedCode.toString().equals(String.valueOf(request.authenticationCode())))) {
            return false;
        }
        if (userRepository.existsByUserLoginId(request.userId())) {
            throw new UserNotFoundException(request.userId());
        }
        User user = userRepository.findByUserLoginId(request.userId()).get();
        user.setState(State.ACTIVATE);

        redisTemplate.delete(key);
        return true;
    }

    public TokenPayloadResponse getTokenPayload(TokenPayloadRequest tokenPayloadRequest) {
        Claims claims = jwtUtil.parseToken(tokenPayloadRequest.token());
        return new TokenPayloadResponse(claims);
    }

    public void updateLastLogin(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.setLastLoginAt(LocalDate.now());
    }
}
