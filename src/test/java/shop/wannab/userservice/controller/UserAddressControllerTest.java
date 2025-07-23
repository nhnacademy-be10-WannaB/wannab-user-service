package shop.wannab.userservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import shop.wannab.userservice.address.controller.UserAddressController;
import shop.wannab.userservice.address.domain.dto.request.UserAddressCreateRequest;
import shop.wannab.userservice.address.domain.dto.request.UserAddressUpdateRequest;
import shop.wannab.userservice.address.domain.dto.response.UserAddressResponse;
import shop.wannab.userservice.address.exception.AlreadyExistsUserAddressException;
import shop.wannab.userservice.address.exception.UserAddressFullException;
import shop.wannab.userservice.address.exception.UserAddressNotFoundException;
import shop.wannab.userservice.address.service.UserAddressService;

@ActiveProfiles("ci")
@AutoConfigureRestDocs
@DisplayName("UserAddress Controller 단위 테스트")
@WebMvcTest(UserAddressController.class)
class UserAddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserAddressService userAddressService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("모든 주소 조회 - 성공")
    void getAllAddresses_success() throws Exception {
        given(userAddressService.findByUserId(1L)).willReturn(List.of(
                UserAddressResponse.builder()
                        .addressId(1L)
                        .addressName("집")
                        .address("서울시 강남구")
                        .detailAddress("101동 202호")
                        .build()
        ));

        mockMvc.perform(get("/api/users/addresses")
                        .header("X-USER-ID", 1L))
                .andExpect(status().isOk())
                .andDo(document("address/get-all",
                        requestHeaders(
                                headerWithName("X-USER-ID").description("유저 ID")
                        ),
                        responseFields(
                                fieldWithPath("[].addressId").description("주소 ID"),
                                fieldWithPath("[].addressName").description("주소 별칭"),
                                fieldWithPath("[].address").description("주소"),
                                fieldWithPath("[].detailAddress").description("상세 주소")
                        )
                ));
    }

    @Test
    @DisplayName("주소 상세 조회 - 성공")
    void getAddress_success() throws Exception {
        given(userAddressService.findByUserIdAndAddressId(1L, 100L))
                .willReturn(UserAddressResponse.builder().build());

        mockMvc.perform(get("/api/users/addresses/{address-id}", 100L)
                        .header("X-USER-ID", 1L))
                .andExpect(status().isOk())
                .andDo(document("address/get-address",
                        requestHeaders(
                                headerWithName("X-USER-ID").description("사용자 ID")
                        ),
                        pathParameters(
                                parameterWithName("address-id").description("주소 ID")
                        ),
                        responseFields(
                                fieldWithPath("addressId").description("주소 ID"),
                                fieldWithPath("addressName").description("주소 이름"),
                                fieldWithPath("address").description("주소"),
                                fieldWithPath("detailAddress").description("상세 주소")
                        )
                ));
    }

    @Test
    @DisplayName("주소 상세 조회 - 존재하지 않을 경우")
    void getAddress_notFound() throws Exception {
        given(userAddressService.findByUserIdAndAddressId(anyLong(), anyLong()))
                .willThrow(new UserAddressNotFoundException("주소 없음"));

        mockMvc.perform(get("/api/users/addresses/100")
                        .header("X-USER-ID", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("01002"))
                .andExpect(jsonPath("$.message").value("주소 없음"));
    }


    @Test
    @DisplayName("주소 등록 - 성공")
    void createAddress_success() throws Exception {
        UserAddressCreateRequest request = new UserAddressCreateRequest("지후 집", "동림초등학교", "운동장");

        mockMvc.perform(post("/api/users/addresses")
                        .header("X-USER-ID", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("address/create-address",
                        requestHeaders(
                                headerWithName("X-USER-ID").description("사용자 ID")
                        ),
                        requestFields(
                                fieldWithPath("addressName").description("주소 이름"),
                                fieldWithPath("address").description("주소"),
                                fieldWithPath("detailAddress").description("상세 주소")
                        ),
                        responseFields(
                                fieldWithPath("data").description("응답 데이터").optional(),
                                fieldWithPath("responseCode").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지").optional()
                        )
                ));
    }

    @Test
    @DisplayName("주소 등록 - 주소가 이미 존재할 경우")
    void createAddress_alreadyExists() throws Exception {
        willThrow(new AlreadyExistsUserAddressException("이미 존재합니다.")).given(userAddressService)
                .save(anyLong(), any(UserAddressCreateRequest.class));

        UserAddressCreateRequest request = new UserAddressCreateRequest("지후 집", "동림초등학교", "운동장");

        mockMvc.perform(post("/api/users/addresses")
                        .header("X-USER-ID", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("01003"))
                .andExpect(jsonPath("$.message").doesNotExist());
    }


    @Test
    @DisplayName("주소 등록 - 주소 최대 개수 초과")
    void createAddress_full() throws Exception {
        willThrow(new UserAddressFullException("가득참")).given(userAddressService)
                .save(anyLong(), any(UserAddressCreateRequest.class));

        UserAddressCreateRequest request = new UserAddressCreateRequest("지후집2", "GS25", "1층");

        mockMvc.perform(post("/api/users/addresses")
                        .header("X-USER-ID", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("01001"))
                .andExpect(jsonPath("$.message").value("가득참"));
    }

    @Test
    @DisplayName("주소 수정 - 성공")
    void updateAddress_success() throws Exception {
        UserAddressUpdateRequest request = new UserAddressUpdateRequest("지후집2", "GS25", "1층");

        mockMvc.perform(put("/api/users/addresses/{address-id}", 100L)
                        .header("X-USER-ID", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("address/update-address",
                        pathParameters(
                                parameterWithName("address-id").description("수정할 주소 ID")
                        ),
                        requestHeaders(
                                headerWithName("X-USER-ID").description("사용자 ID")
                        ),
                        requestFields(
                                fieldWithPath("addressName").description("주소 이름"),
                                fieldWithPath("address").description("주소"),
                                fieldWithPath("detailAddress").description("상세 주소")
                        ),
                        responseFields(
                                fieldWithPath("data").description("응답 데이터").optional(),
                                fieldWithPath("responseCode").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지").optional()
                        )
                ));
    }

    @Test
    @DisplayName("주소 삭제 - 성공")
    void deleteAddress_success() throws Exception {
        mockMvc.perform(delete("/api/users/addresses/{address-id}", 100L)
                        .header("X-USER-ID", 1L))
                .andExpect(status().isOk())
                .andDo(document("address/delete-address",
                        pathParameters(
                                parameterWithName("address-id").description("삭제할 주소 ID")
                        ),
                        requestHeaders(
                                headerWithName("X-USER-ID").description("사용자 ID")
                        ),
                        responseFields(
                                fieldWithPath("data").description("응답 데이터").optional(),
                                fieldWithPath("responseCode").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지").optional()
                        )
                ));
    }
}
