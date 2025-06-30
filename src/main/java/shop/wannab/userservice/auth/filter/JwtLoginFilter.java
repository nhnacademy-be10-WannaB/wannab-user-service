package shop.wannab.userservice.auth.filter;


import static shop.wannab.userservice.utils.JwtUtil.REFRESH_KEY;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import shop.wannab.userservice.auth.controller.request.LoginRequest;
import shop.wannab.userservice.auth.controller.response.LoginResponse;
import shop.wannab.userservice.auth.domain.CustomUserDetails;
import shop.wannab.userservice.utils.JwtUtil;

@Slf4j
public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final ObjectMapper mapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public JwtLoginFilter(AuthenticationManager authenticationManager, ObjectMapper mapper,
                          RedisTemplate<String, Object> redisTemplate) {
        this.authenticationManager = authenticationManager;
        this.mapper = mapper;
        this.redisTemplate = redisTemplate;
        setFilterProcessesUrl("/api/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        LoginRequest loginRequest = parseRequest(request);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                loginRequest.username(),
                loginRequest.password()
        );
        log.info("로그인 시도 : username : {}, password : {}", loginRequest.username(), loginRequest.password());
        return this.authenticationManager.authenticate(token);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        CustomUserDetails principal = (CustomUserDetails) authResult.getPrincipal();
        String role = principal.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() -> new RuntimeException("권한 없음"));

        String accessToken = JwtUtil.createAccessToken(principal.getId(), role);
        String refreshToken = JwtUtil.createRefreshToken(principal.getId(), role);

        redisTemplate.opsForHash().put(REFRESH_KEY, String.valueOf(principal.getId()), refreshToken);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        LoginResponse loginResponse = new LoginResponse(accessToken, refreshToken);

        mapper.writeValue(response.getWriter(), loginResponse);
    }

    private LoginRequest parseRequest(HttpServletRequest request){
        try {
            return mapper.readValue(request.getInputStream(), LoginRequest.class);
        } catch (IOException e) {
            throw new RuntimeException("로그인 요청이 올바르지 않습니다", e);
        }
    }
}
