package shop.wannab.userservice.controller;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import shop.wannab.userservice.user.controller.UserController;
import shop.wannab.userservice.user.domain.dto.request.UserUpdateRequest;
import shop.wannab.userservice.user.domain.dto.response.UserPageResponse;
import shop.wannab.userservice.user.service.UserService;
import shop.wannab.userservice.utils.HeaderUtil;

@ActiveProfiles("ci")
@AutoConfigureRestDocs
@DisplayName("User Controller 단위 테스트")
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("내 정보 조회 API 테스트")
    void readMyPageUser_success() throws Exception {
        // given
        Long userId = 1L;
        UserPageResponse mockResponse = UserPageResponse.builder()
                .username("testuser")
                .name("홍길동")
                .email("test@test.com")
                .phone("010-1234-5678")
                .birth(LocalDate.of(1995, 5, 5))
                .nickname("nickname")
                .password("encrypted")
                .points(100)
                .grade("Standard")
                .build();

        given(userService.readUserPageResponse(userId)).willReturn(mockResponse);

        // when / then
        mockMvc.perform(get("/api/users")
                        .header(HeaderUtil.HEADER_ID_NAME, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andDo(document("users/read-user",
                        requestHeaders(
                                headerWithName("X-USER-ID").description("회원 고유 ID")
                        ),
                        responseFields(
                                fieldWithPath("username").description("사용자 아이디"),
                                fieldWithPath("name").description("이름"),
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("phone").description("전화번호"),
                                fieldWithPath("birth").description("생년월일"),
                                fieldWithPath("nickname").description("닉네임"),
                                fieldWithPath("password").description("비밀번호"),
                                fieldWithPath("points").description("포인트"),
                                fieldWithPath("grade").description("회원 등급")
                        )
                ));

    }

    @Test
    @DisplayName("내 정보 수정 API 테스트")
    void updateUser_success() throws Exception {
        // given
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest(
                "newPass", "새이름", "new@test.com", "newnick", "01088889999");

        UserPageResponse mockResponse = UserPageResponse.builder()
                .username("testuser")
                .name("새이름")
                .email("new@test.com")
                .phone("010-8888-9999")
                .birth(LocalDate.of(1990, 1, 1))
                .nickname("newnick")
                .password("encrypted")
                .points(0)
                .grade("Standard")
                .build();

        given(userService.updateUser(eq(userId), any(UserUpdateRequest.class)))
                .willReturn(mockResponse);

        // when / then
        mockMvc.perform(post("/api/users")
                        .header(HeaderUtil.HEADER_ID_NAME, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("새이름"))
                .andExpect(jsonPath("$.nickname").value("newnick"))
                .andDo(document("users/update-user",
                        requestHeaders(
                                headerWithName("X-USER-ID").description("회원 고유 ID")
                        ),
                        requestFields(
                                fieldWithPath("password").description("새 비밀번호"),
                                fieldWithPath("name").description("새 이름"),
                                fieldWithPath("email").description("새 이메일"),
                                fieldWithPath("nickname").description("새 닉네임"),
                                fieldWithPath("phone").description("새 전화번호")
                        ),
                        responseFields(
                                fieldWithPath("username").description("회원 아이디"),
                                fieldWithPath("name").description("이름"),
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("phone").description("전화번호"),
                                fieldWithPath("birth").description("생년월일"),
                                fieldWithPath("nickname").description("닉네임"),
                                fieldWithPath("password").description("비밀번호"),
                                fieldWithPath("points").description("회원 포인트"),
                                fieldWithPath("grade").description("회원 등급")
                        )
                ));
    }

    @Test
    @DisplayName("회원 탈퇴 API 테스트")
    void deleteUser_success() throws Exception {
        // given
        Long userId = 1L;
        willDoNothing().given(userService).deleteUser(userId);

        // when / then
        mockMvc.perform(delete("/api/users")
                        .header(HeaderUtil.HEADER_ID_NAME, userId))
                .andExpect(status().isNoContent())
                .andDo(document("users/delete-user",
                        requestHeaders(
                                headerWithName(HeaderUtil.HEADER_ID_NAME).description("회원 고유 ID")
                        )
                ));
    }

    @Test
    @DisplayName("로그아웃 API 테스트")
    void logout_success() throws Exception {
        // given
        Long userId = 1L;
        willDoNothing().given(userService).logout(userId);

        // when / then
        mockMvc.perform(get("/api/users/logout")
                        .header(HeaderUtil.HEADER_ID_NAME, userId))
                .andExpect(status().isNoContent())
                .andDo(document("users/logout",
                        requestHeaders(
                                headerWithName(HeaderUtil.HEADER_ID_NAME).description("회원 고유 ID")
                        )
                ));

    }

    @Test
    @DisplayName("생일자 목록 조회 API 테스트")
    void birthUserList_success() throws Exception {
        // given
        int month = 7;
        List<Long> mockIds = List.of(1L, 2L, 3L);
        given(userService.birthUserList(month)).willReturn(mockIds);

        // when / then
        mockMvc.perform(get("/api/users/birthdays")
                        .param("month", String.valueOf(month)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andDo(document("users/birthday-list",
                        queryParameters(
                                parameterWithName("month").description("조회할 생일 월 (1~12)")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("해당 월에 생일이 있는 사용자 ID 목록")
                        )
                ));
    }

}