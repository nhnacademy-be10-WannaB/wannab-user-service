package shop.wannab.userservice.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import shop.wannab.userservice.auth.controller.AuthController;
import shop.wannab.userservice.auth.dto.request.PaycoLoginRequest;
import shop.wannab.userservice.auth.dto.request.ReissueRequest;
import shop.wannab.userservice.auth.dto.request.TokenPayloadRequest;
import shop.wannab.userservice.auth.dto.request.TokenRequest;
import shop.wannab.userservice.auth.dto.request.UnlockRequest;
import shop.wannab.userservice.auth.dto.response.PaycoLoginResponse;
import shop.wannab.userservice.auth.dto.response.ReissueResponse;
import shop.wannab.userservice.auth.dto.response.TokenResponse;
import shop.wannab.userservice.auth.dto.response.UserResponse;
import shop.wannab.userservice.auth.service.AuthService;
import shop.wannab.userservice.global.Response;
import shop.wannab.userservice.point.service.PointHistoryService;
import shop.wannab.userservice.user.domain.dto.request.UserCreateRequest;
import shop.wannab.userservice.user.domain.entity.Role;
import shop.wannab.userservice.user.domain.entity.State;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.domain.entity.UserGrade;
import shop.wannab.userservice.user.service.UserService;
import shop.wannab.userservice.utils.ResponseCode;

@ActiveProfiles("ci")
@AutoConfigureRestDocs
@DisplayName("Auth Controller 단위 테스트")
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    @MockBean
    private PointHistoryService pointHistoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private final UserGrade mockGrade = mock(UserGrade.class);

    @DisplayName("토큰 재발급 API 테스트")
    @Test
    void refreshAccessToken_success() throws Exception {
        // given
        String refreshToken = "mock-refresh-token";
        ReissueRequest request = new ReissueRequest(refreshToken);

        ReissueResponse mockResponse = new ReissueResponse("new-access-token");

        given(userService.reissueToken(refreshToken)).willReturn(mockResponse);

        // when / then
        mockMvc.perform(post("/api/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andDo(document("auth/reissue",
                        requestFields(
                                fieldWithPath("refreshToken").description("리프레시 토큰")
                        ),
                        responseFields(
                                fieldWithPath("accessToken").description("재발급된 액세스 토큰")
                        )
                ));
    }

    @DisplayName("로그인 API 테스트")
    @Test
    void login_success() throws Exception {
        // given
        TokenRequest request = new TokenRequest(1l, "password123");

        TokenResponse response = new TokenResponse(
                "mock-access-token",
                "mock-refresh-token"
        );

        given(authService.login(any(TokenRequest.class))).willReturn(response);

        // when / then
        mockMvc.perform(post("/api/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"))
                .andDo(document("auth/login",
                        requestFields(
                                fieldWithPath("userId").description("회원 고유 ID"),
                                fieldWithPath("role").description("사용자 역할 또는 비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("accessToken").description("JWT 액세스 토큰"),
                                fieldWithPath("refreshToken").description("JWT 리프레시 토큰")
                        )
                ));

    }

    @DisplayName("로그인 ID로 사용자 조회 API 테스트")
    @Test
    void getUserByLoginId_success() throws Exception {
        // given
        String loginId = "testuser";
        UserResponse mockResponse = new UserResponse(
                1L,
                "testuser",
                State.ACTIVATE
        );

        given(userService.readUserResponse(loginId)).willReturn(mockResponse);

        // when / then
        mockMvc.perform(get("/api/auth/users")
                        .param("loginId", loginId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.loginId").value("testuser"))
                .andExpect(jsonPath("$.state").value(State.ACTIVATE.name()))

                .andDo(document("auth/find-user-by-login-id",
                        queryParameters(
                                parameterWithName("loginId").description("로그인 아이디")
                        ),
                        responseFields(
                                fieldWithPath("userId").description("회원 고유 ID"),
                                fieldWithPath("loginId").description("로그인 아이디"),
                                fieldWithPath("state").description("상태")
                        )
                ));

    }


    @Test
    @DisplayName("PAYCO 로그인 성공")
    void paycoLogin_success() throws Exception {
        // given
        PaycoLoginRequest request = new PaycoLoginRequest(
                "payco-id", "PAYCO", "user@example.com",
                LocalDate.of(1990, 1, 1), "010-1234-5678", "홍길동");

        PaycoLoginResponse responseData = new PaycoLoginResponse(1L, Role.USER.name());
        Response<PaycoLoginResponse> response = new Response<>(responseData, ResponseCode.PAYCO_LOGIN_SUCESS, "성공");

        when(authService.paycoLogin(any(PaycoLoginRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login/payco")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.message").value("성공"))
                .andDo(document("auth/payco-login",
                        requestFields(
                                fieldWithPath("providerId").description("PAYCO 사용자 식별 ID"),
                                fieldWithPath("providerName").description("소셜 로그인 제공자 (예: PAYCO)"),
                                fieldWithPath("email").description("사용자 이메일"),
                                fieldWithPath("birthday").description("생년월일 (yyyy-MM-dd)"),
                                fieldWithPath("phone").description("전화번호"),
                                fieldWithPath("name").description("이름")
                        ),
                        responseFields(
                                fieldWithPath("data.id").description("회원 고유 ID"),
                                fieldWithPath("data.role").description("회원 역할"),
                                fieldWithPath("responseCode").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지")
                        )
                ));

    }

    @Test
    @DisplayName("토큰 정보 조회 성공")
    void tokenInfo_success() throws Exception {
        // given
        String token = "mock.jwt.token";
        TokenPayloadRequest request = new TokenPayloadRequest(token);

        when(authService.getTokenPayload(any())).thenReturn(
                new shop.wannab.userservice.auth.dto.response.TokenPayloadResponse(null));

        // when & then
        mockMvc.perform(post("/api/auth/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("auth/token-info",
                        requestFields(
                                fieldWithPath("token").description("access_token")
                        ),
                        responseFields(
                                fieldWithPath("claims").optional().description("Map<String, Object>")
                        )
                ));
    }

    @Test
    @DisplayName("회원가입 성공 시 포인트 적립도 수행됨")
    void signup_success() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "testId", "pw1234", "010-9999-8888", "test@ex.com",
                "01011111111", LocalDate.of(1990, 1, 1));

        User newUser = User.standard()
                .password("pw1234")
                .userLoginId("testId")
                .name("홍길동")
                .email("test@ex.com")
                .phone("010-9999-8888")
                .birth(LocalDate.of(1990, 1, 1))
                .userGrade(mockGrade)
                .build();

        when(userService.createUser(any())).thenReturn(newUser);
        doNothing().when(pointHistoryService).createSignupPoints(any());

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("auth/signup",
                        requestFields(
                                fieldWithPath("userLoginId").description("사용자 로그인 ID"),
                                fieldWithPath("password").description("비밀번호"),
                                fieldWithPath("phone").description("전화번호"),
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("name").description("이름"),
                                fieldWithPath("birth").description("생년월일 (yyyy-MM-dd)")
                        ),
                        responseFields(
                                fieldWithPath("data").description("응답 데이터 (null)").optional(),
                                fieldWithPath("responseCode").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지").optional()
                        )
                ));

        verify(pointHistoryService).createSignupPoints(newUser);
    }

    @Test
    @DisplayName("마지막 로그인 시간 업데이트")
    void updateLastLogin_success() throws Exception {
        // given
        Long userId = 10L;
        doNothing().when(authService).updateLastLogin(userId);

        // when & then
        mockMvc.perform(put("/api/auth/lastLogin")
                        .queryParam("userId", String.valueOf(userId)))
                .andExpect(status().isNoContent())
                .andDo(document("auth/update-last-login",
                        queryParameters(
                                parameterWithName("userId").description("로그인한 사용자 ID")
                        )
                ));

        verify(authService).updateLastLogin(userId);
    }

    @Test
    @DisplayName("아이디 중복 체크 - 사용 가능")
    void checkDuplicated_false() throws Exception {
        // given
        when(userService.duplicated("newId")).thenReturn(false);

        // when & then
        mockMvc.perform(get("/api/auth/duplicated")
                        .param("id", "newId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("잠금 해제 요청 성공")
    void unlockRequest_success() throws Exception {
        doNothing().when(authService).unlockRequest("userId123");

        mockMvc.perform(post("/api/auth/unlock/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"userId123\""))
                .andExpect(status().isOk())
                .andExpect(content().string("\"userId123\""));
    }

    @Test
    @DisplayName("잠금 해제 검증 성공")
    void unlockVerify_success() throws Exception {
        UnlockRequest request = new UnlockRequest("userId123", 123);
        when(authService.unlock(any())).thenReturn(true);

        mockMvc.perform(post("/api/auth/unlock/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"))
                .andDo(document("auth/unlock-verify",
                        requestFields(
                                fieldWithPath("userId").description("잠금 해제를 요청한 사용자 ID"),
                                fieldWithPath("authenticationCode").description("인증 코드")
                        )

                ));
    }

}
