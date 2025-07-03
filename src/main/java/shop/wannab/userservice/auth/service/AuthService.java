package shop.wannab.userservice.auth.service;

import static shop.wannab.userservice.utils.JwtUtil.REFRESH_KEY;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import shop.wannab.userservice.auth.controller.request.TokenRequest;
import shop.wannab.userservice.auth.controller.response.TokenResponse;
import shop.wannab.userservice.utils.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    public TokenResponse login(TokenRequest tokenRequest) {
        String accessToken = jwtUtil.createAccessToken(tokenRequest.userId(), tokenRequest.role().name());
        String refreshToken = jwtUtil.createRefreshToken(tokenRequest.userId(), tokenRequest.role().name());
        redisTemplate.opsForHash().put(REFRESH_KEY, String.valueOf(tokenRequest.userId()), refreshToken);
        return new TokenResponse(accessToken, refreshToken);
    }
}
