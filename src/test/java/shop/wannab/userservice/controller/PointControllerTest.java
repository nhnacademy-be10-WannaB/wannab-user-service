package shop.wannab.userservice.controller;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import shop.wannab.userservice.point.controller.PointController;
import shop.wannab.userservice.point.domain.dto.request.PointPolicyCreateRequest;
import shop.wannab.userservice.point.domain.dto.request.PointPolicyUpdateDTO;
import shop.wannab.userservice.point.domain.dto.request.PointUpdateDTO;
import shop.wannab.userservice.point.domain.dto.response.PointHistoryResponse;
import shop.wannab.userservice.point.domain.entity.PointHistory;
import shop.wannab.userservice.point.domain.entity.PointPolicy;
import shop.wannab.userservice.point.service.PointHistoryService;
import shop.wannab.userservice.point.service.PointPolicyService;
import shop.wannab.userservice.user.domain.entity.Role;
import shop.wannab.userservice.user.service.UserService;

@ActiveProfiles("dev")
@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private PointHistoryService pointHistoryService;

    @MockBean
    private PointPolicyService pointPolicyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("포인트 조회 성공")
    void testReadPoints() throws Exception {
        when(userService.readPoint(1L)).thenReturn(1000);

        mockMvc.perform(get("/api/users/points")
                        .header("X-USER-ID", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("1000"));
    }

    @Test
    @DisplayName("포인트 수정 성공")
    void testUpdatePoints() throws Exception {
        PointUpdateDTO dto = new PointUpdateDTO(300);
        mockMvc.perform(post("/api/users/points")
                        .header("X-USER-ID", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
        verify(userService).updatePoint(eq(1L), any());
    }

    @Test
    @DisplayName("포인트 히스토리 조회 성공")
    void testReadPointHistories() throws Exception {
        PointHistory pointHistory = PointHistory.builder()
                .pointHistoryReason("리뷰 적립")
                .pointHistoryChange(300)
                .totalPoints(1000)
                .build();

        List<PointHistoryResponse> histories = List.of(new PointHistoryResponse(pointHistory));
        PageImpl<PointHistoryResponse> page = new PageImpl<>(histories, PageRequest.of(0, 10), 1);

        when(pointHistoryService.readPointHistories(1L, 0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/users/point-histories")
                        .header("X-USER-ID", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].pointHistoryReason").value("리뷰 적립"));
    }

    @Test
    @DisplayName("포인트 정책 조회")
    void testReadPointPolicy() throws Exception {
        when(pointPolicyService.readPointPolicies(Role.ADMIN)).thenReturn(List.of(new PointPolicy()));

        mockMvc.perform(get("/api/reward-rates")
                        .header("X-USER-ROLE", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("포인트 정책 생성")
    void testCreatePointPolicy() throws Exception {
        PointPolicyCreateRequest request = new PointPolicyCreateRequest("REVIEW", 10, 10);
        when(pointPolicyService.createPointPolicy(eq(Role.ADMIN), any())).thenReturn(new PointPolicy());

        mockMvc.perform(post("/api/reward-rates")
                        .header("X-USER-ROLE", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("포인트 정책 수정")
    void testUpdatePointPolicy() throws Exception {
        PointPolicyUpdateDTO dto = new PointPolicyUpdateDTO("REVIEW", 15, 15, true);
        when(pointPolicyService.updatePointPolicy(eq(Role.ADMIN), any())).thenReturn(new PointPolicy());

        mockMvc.perform(put("/api/reward-rates")
                        .header("X-USER-ROLE", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주문 취소시 포인트 반환 처리")
    void testCancelOrderPoint() throws Exception {
        mockMvc.perform(post("/api/users/points/orders/123/cancel"))
                .andExpect(status().isOk());

        verify(pointHistoryService).cancel(123L);
    }

    @Test
    @DisplayName("환불 시 포인트 반환 및 차감")
    void testRefundPoints() throws Exception {
        mockMvc.perform(post("/api/users/points/refund")
                        .param("order-id", "123")
                        .param("amount", "100"))
                .andExpect(status().isOk());

        verify(pointHistoryService).refund(123L, 100);
    }

    @Test
    @DisplayName("리뷰 작성시 포인트 적립")
    void testCreateReviewPoints() throws Exception {
        mockMvc.perform(post("/api/points/reviews")
                        .param("userId", "1"))
                .andExpect(status().isOk());

        verify(pointHistoryService).createReviewPoints(1L);
    }
}