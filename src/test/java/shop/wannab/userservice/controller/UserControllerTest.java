package shop.wannab.userservice.controller;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import shop.wannab.userservice.user.controller.UserController;
import shop.wannab.userservice.user.domain.dto.request.UserUpdateRequest;
import shop.wannab.userservice.user.domain.dto.response.UserPageResponse;
import shop.wannab.userservice.user.service.UserService;
import shop.wannab.userservice.utils.HeaderUtil;

@DisplayName("UserController 테스트")
@WebMvcTest(UserController.class)
@ActiveProfiles("ci")
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
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users")
                        .header(HeaderUtil.HEADER_ID_NAME, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
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
        mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .header(HeaderUtil.HEADER_ID_NAME, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("새이름"))
                .andExpect(jsonPath("$.nickname").value("newnick"));
    }

    @Test
    @DisplayName("회원 탈퇴 API 테스트")
    void deleteUser_success() throws Exception {
        // given
        Long userId = 1L;
        willDoNothing().given(userService).deleteUser(userId);

        // when / then
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users")
                        .header(HeaderUtil.HEADER_ID_NAME, userId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("로그아웃 API 테스트")
    void logout_success() throws Exception {
        // given
        Long userId = 1L;
        willDoNothing().given(userService).logout(userId);

        // when / then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/logout")
                        .header(HeaderUtil.HEADER_ID_NAME, userId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("생일자 목록 조회 API 테스트")
    void birthUserList_success() throws Exception {
        // given
        int month = 7;
        List<Long> mockIds = List.of(1L, 2L, 3L);

        given(userService.birthUserList(month)).willReturn(mockIds);

        // when / then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/birthdays")
                        .param("month", String.valueOf(month)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }
}