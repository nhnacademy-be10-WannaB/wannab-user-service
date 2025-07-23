package shop.wannab.userservice.controller;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseBody;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
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

@ActiveProfiles("ci")
@AutoConfigureRestDocs
@DisplayName("Point Controller 단위 테스트")
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
                .andExpect(content().string("1000"))
                .andDo(document("users/read-points",
                        requestHeaders(
                                headerWithName("X-USER-ID").description("사용자 식별자")
                        ),
                        responseBody()
                ));
    }

    @Test
    @DisplayName("포인트 수정 성공")
    void testUpdatePoints() throws Exception {
        PointUpdateDTO dto = new PointUpdateDTO(300);
        mockMvc.perform(post("/api/users/points")
                        .header("X-USER-ID", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent())
                .andDo(document("users/update-points",
                        requestHeaders(
                                headerWithName("X-USER-ID").description("사용자 식별자")
                        ),
                        requestFields(
                                fieldWithPath("amount").description("적용할 포인트 값")
                        )
                ));
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
                        .header("X-USER-ID", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].pointHistoryReason").value("리뷰 적립"))
                .andDo(document("users/read-point-histories",
                        requestHeaders(
                                headerWithName("X-USER-ID").description("사용자 식별자")
                        ),
                        queryParameters(
                                parameterWithName("page").description("조회할 페이지 번호 (0부터 시작)"),
                                parameterWithName("size").description("페이지 당 항목 수")
                        ),
                        responseFields(
                                fieldWithPath("content[].pointHistoryReason").description("포인트 적립/차감 사유"),
                                fieldWithPath("content[].pointHistoryChange").description("변경된 포인트 값"),
                                fieldWithPath("content[].totalPoints").description("변경 후 누적 포인트"),
                                fieldWithPath("content[].createdAt").description("포인트 변경 일시"),
                                fieldWithPath("content[].pointsHistoryId").optional().description("포인트 히스토리 ID"),

                                fieldWithPath("totalElements").description("전체 포인트 히스토리 수"),
                                fieldWithPath("totalPages").description("전체 페이지 수"),
                                fieldWithPath("number").description("현재 페이지 번호"),
                                fieldWithPath("size").description("페이지 당 항목 수"),
                                fieldWithPath("hasNext").description("다음 페이지 존재 여부"),
                                fieldWithPath("hasPrevious").description("이전 페이지 존재 여부")
                        )
                ));

    }

    @Test
    @DisplayName("포인트 정책 조회")
    void testReadPointPolicy() throws Exception {
        when(pointPolicyService.readPointPolicies(Role.ADMIN)).thenReturn(List.of(new PointPolicy()));

        mockMvc.perform(get("/api/reward-rates")
                        .header("X-USER-ROLE", "ADMIN"))
                .andExpect(status().isOk())
                .andDo(document("point-policy/read",
                        requestHeaders(
                                headerWithName("X-USER-ROLE").description("요청자의 권한 (예: ADMIN)")
                        ),
                        responseFields(
                                fieldWithPath("[].id").description("포인트 정책 ID").optional(),
                                fieldWithPath("[].policyName").description("포인트 정책 이름").optional(),
                                fieldWithPath("[].addRate").description("추가 리워드 비율 (%)").optional(),
                                fieldWithPath("[].addPoint").description("추가 포인트").optional(),
                                fieldWithPath("[].active").description("정책 활성화 여부").optional()
                        )
                ));
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
                .andExpect(status().isCreated())
                .andDo(document("point-policy/read",
                        requestHeaders(
                                headerWithName("X-USER-ROLE").description("요청자의 권한 (예: ADMIN)")
                        ),
                        requestFields(
                                fieldWithPath("name").description("포인트 정책 이름 (예: REVIEW)"),
                                fieldWithPath("addRate").description("포인트 추가율 (0~100, 비율 단위)"),
                                fieldWithPath("addPoint").description("포인트 추가 값 (정수)")
                        ),
                        responseFields(
                                fieldWithPath("id").description("포인트 정책 ID"),
                                fieldWithPath("policyName").description("포인트 정책 이름"),
                                fieldWithPath("addRate").description("포인트 추가율 (0~100)"),
                                fieldWithPath("addPoint").description("포인트 추가 포인트"),
                                fieldWithPath("active").description("정책 활성화 여부")
                        )
                ));


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
                .andExpect(status().isOk())
                .andDo(document("point-policy/update",
                        requestHeaders(
                                headerWithName("X-USER-ROLE").description("요청자의 권한 (예: ADMIN)")
                        ),
                        requestFields(
                                fieldWithPath("name").description("포인트 정책 이름"),
                                fieldWithPath("addRate").description("포인트 추가율 (0~100)"),
                                fieldWithPath("addPoint").description("포인트 추가 포인트"),
                                fieldWithPath("active").description("정책 활성화 여부")
                        ),
                        responseFields(
                                fieldWithPath("id").description("포인트 정책 ID"),
                                fieldWithPath("policyName").description("포인트 정책 이름"),
                                fieldWithPath("addRate").description("포인트 추가율 (0~100)"),
                                fieldWithPath("addPoint").description("포인트 추가 포인트"),
                                fieldWithPath("active").description("정책 활성화 여부")
                        )
                ));

    }

    @Test
    @DisplayName("주문 취소시 포인트 반환 처리")
    void testCancelOrderPoint() throws Exception {
        mockMvc.perform(post("/api/users/points/orders/{order-id}/cancel", 123L))

                .andExpect(status().isOk())
                .andDo(document("point-history/cancel-order",
                        pathParameters(
                                parameterWithName("order-id").description("주문 ID")
                        )
                ));

        verify(pointHistoryService).cancel(123L);
    }


    @Test
    @DisplayName("환불 시 포인트 반환 및 차감")
    void testRefundPoints() throws Exception {
        mockMvc.perform(post("/api/users/points/refund")
                        .queryParam("order-id", "123")
                        .queryParam("amount", "100"))
                .andExpect(status().isOk())
                .andDo(document("point-history/refund",
                        queryParameters(
                                parameterWithName("order-id").description("주문 ID"),
                                parameterWithName("amount").description("환불 받을 포인트 금액")
                        )
                ));

        verify(pointHistoryService).refund(123L, 100);
    }

    @Test
    @DisplayName("리뷰 작성시 포인트 적립")
    void testCreateReviewPoints() throws Exception {
        mockMvc.perform(post("/api/points/reviews")
                        .queryParam("userId", "1"))
                .andExpect(status().isOk())
                .andDo(document("point-history/create-review",
                        queryParameters(
                                parameterWithName("userId").description("리뷰를 작성한 사용자 ID")
                        )
                ));

        verify(pointHistoryService).createReviewPoints(1L);
    }
}