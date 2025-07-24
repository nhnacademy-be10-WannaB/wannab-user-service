package shop.wannab.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import shop.wannab.userservice.point.domain.dto.request.PointHistoryCreateDTO;
import shop.wannab.userservice.point.domain.dto.request.PointHistoryRollbackPointDTO;
import shop.wannab.userservice.point.domain.dto.request.PointUpdateDTO;
import shop.wannab.userservice.point.domain.dto.response.PointHistoryResponse;
import shop.wannab.userservice.point.domain.entity.PointHistory;
import shop.wannab.userservice.point.domain.entity.PointPolicy;
import shop.wannab.userservice.point.repository.PointHistoryRepository;
import shop.wannab.userservice.point.repository.PointPolicyRepository;
import shop.wannab.userservice.point.service.PointHistoryServiceImpl;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.domain.entity.UserGrade;
import shop.wannab.userservice.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class PointHistoryServiceTest {

    @InjectMocks
    private PointHistoryServiceImpl pointHistoryService;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Mock
    private PointPolicyRepository pointPolicyRepository;

    @Mock
    private UserService userService;

    private final Long userId = 1L;
    private final Long orderId = 100L;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.standard()
                .password("password123")
                .userLoginId("testUser")
                .name("테스트")
                .email("test@example.com")
                .phone("01012345678")
                .birth(LocalDate.of(1990, 1, 1))
                .userGrade(new UserGrade(1L, "VIP", 0.05))  // userGrade는 직접 객체 생성 또는 mock
                .build();
        ReflectionTestUtils.setField(user, "userId", userId);
        user.setPoints(1000);

    }

    @DisplayName("포인트 적립 내역 생성 - 포인트 사용 없이")
    @Test
    void createPointHistory_withoutUsedPoints_success() {
        // given
        PointHistoryCreateDTO dto = new PointHistoryCreateDTO(userId, 0, 20000, orderId);
        given(userService.readUser(userId)).willReturn(user);
        given(pointPolicyRepository.findByPolicyName("기본적립률")).willReturn(Optional.empty());
        given(pointHistoryRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        PointHistory result = pointHistoryService.createPointHistory(dto);

        // then
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getPointHistoryReason()).isEqualTo("도서구매");
        assertThat(result.getPointHistoryChange()).isPositive();
    }

    @DisplayName("포인트 사용 내역 생성 - 잔여 포인트 부족으로 실패")
    @Test
    void createPointHistory_usedPoints_negativeTotal_throwException() {
        // given
        PointHistoryCreateDTO dto = new PointHistoryCreateDTO(userId, 10000, 1500, orderId); // 1000 보유, 1500 사용

        // when & then
        Assertions.assertThrows(RuntimeException.class, () -> pointHistoryService.createPointHistory(dto));
    }

    @DisplayName("리뷰 작성 포인트 생성 - 정책 존재")
    @Test
    void createReviewPoints_success() {
        // given
        PointPolicy policy = PointPolicy.builder()
                .policyName("리뷰작성")
                .addPoint(200)
                .addRate(0) // 미사용
                .build();

        given(userService.readUser(userId)).willReturn(user);
        given(pointPolicyRepository.findByPolicyName("리뷰작성")).willReturn(Optional.of(policy));

        // when
        pointHistoryService.createReviewPoints(userId);

        // then
        verify(pointHistoryRepository).save(any());
        verify(userService).updatePoint(eq(userId), any(PointUpdateDTO.class));
    }

    @DisplayName("회원가입 포인트 생성 - 정책 존재")
    @Test
    void createSignupPoints_success() {
        // given
        PointPolicy policy = PointPolicy.builder()
                .policyName("회원가입")
                .addPoint(1000)
                .addRate(0) // 미사용
                .build();
        given(pointPolicyRepository.findByPolicyName("회원가입")).willReturn(Optional.of(policy));

        // when
        pointHistoryService.createSignupPoints(user);

        // then
        verify(pointHistoryRepository).save(any());
        verify(userService).updatePoint(eq(userId), any(PointUpdateDTO.class));
    }

    @DisplayName("도서 구매 시 포인트 사용 + 기본 적립률 정책 적용 - 성공")
    @Test
    void createPointHistory_withUsedPoints_andPolicy_success() {
        // given
        int usedPoints = 200;
        int orderTotalPrice = 10000;

        UserGrade grade = new UserGrade(); // reward_rate 0.01 (1%)
        ReflectionTestUtils.setField(grade, "rewardRate", 0.01);
        user = User.standard()
                .userLoginId("user1")
                .password("pass")
                .name("테스트유저")
                .email("test@abc.com")
                .phone("010-1234-5678")
                .birth(LocalDate.of(1990, 1, 1))
                .userGrade(grade)
                .build();
        ReflectionTestUtils.setField(user, "userId", userId);
        user.setPoints(1000);

        PointPolicy policy = PointPolicy.builder()
                .policyName("기본적립률")
                .addRate(2) // 2%
                .addPoint(0)
                .build();

        PointHistoryCreateDTO dto = new PointHistoryCreateDTO(userId, usedPoints, orderTotalPrice, orderId);

        given(userService.readUser(userId)).willReturn(user);
        given(pointPolicyRepository.findByPolicyName("기본적립률")).willReturn(Optional.of(policy));
        given(pointHistoryRepository.save(any(PointHistory.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        PointHistory result = pointHistoryService.createPointHistory(dto);

        // then
        assertThat(result.getPointHistoryReason()).isEqualTo("도서구매");
        assertThat(result.getPointHistoryChange()).isPositive();
        assertThat(result.getTotalPoints()).isPositive();
        verify(userService, times(2)).updatePoint(eq(userId), any(PointUpdateDTO.class));
        verify(pointHistoryRepository, times(2)).save(any(PointHistory.class)); // 사용 + 적립
    }


    @DisplayName("포인트 내역 조회 - 성공")
    @Test
    void readPointHistories_success() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        PointHistory pointHistory = PointHistory.builder().user(user).build();
        Page<PointHistory> page = new PageImpl<>(List.of(pointHistory), pageable, 1);
        given(userService.readUser(userId)).willReturn(user);
        given(pointHistoryRepository.findAllByUser(user, pageable)).willReturn(page);

        // when
        Page<PointHistoryResponse> result = pointHistoryService.readPointHistories(userId, 0, 10);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(pointHistoryRepository).findAllByUser(user, pageable);
    }

    @DisplayName("주문 취소에 따른 포인트 내역 회수 생성")
    @Test
    void rollbackPointHistory_success() {
        // given
        PointHistoryRollbackPointDTO dto = PointHistoryRollbackPointDTO.builder()
                .user(user)
                .orderId(orderId)
                .pointHistoryChange(-100)
                .pointHistoryReason("포인트 회수")
                .totalPoints(900)
                .build();

        // when
        pointHistoryService.rollbackPointHistory(dto);

        // then
        verify(pointHistoryRepository).save(any(PointHistory.class));
    }

    @DisplayName("포인트 회수 및 복원 - cancel()")
    @Test
    void cancel_success() {
        // given
        PointHistory earn = PointHistory.builder()
                .user(user)
                .orderId(orderId)
                .pointHistoryChange(200)
                .pointHistoryReason("도서구매")
                .build();

        PointHistory used = PointHistory.builder()
                .user(user)
                .orderId(orderId)
                .pointHistoryChange(100)
                .pointHistoryReason("도서구매 포인트 사용")
                .build();

        given(pointHistoryRepository.findPointHistoriesByOrderId(orderId)).willReturn(List.of(earn, used));
        given(userService.readUser(userId)).willReturn(user);

        // when
        pointHistoryService.cancel(orderId);

        // then
        verify(pointHistoryRepository, times(2)).save(any(PointHistory.class));
        verify(userService, times(2)).updatePoint(eq(userId), any(PointUpdateDTO.class));
    }

    @DisplayName("포인트 환불 및 회수/복원까지 수행 - refund()")
    @Test
    void refund_success() {
        // given
        PointHistory earn = PointHistory.builder()
                .user(user)
                .orderId(orderId)
                .pointHistoryChange(300)
                .pointHistoryReason("도서구매")
                .build();

        given(pointHistoryRepository.findPointHistoriesByOrderId(orderId)).willReturn(List.of(earn));
        given(userService.readUser(userId)).willReturn(user);

        // when
        pointHistoryService.refund(orderId, 300);

        // then
        verify(pointHistoryRepository, atLeastOnce()).save(any(PointHistory.class));
        verify(userService, atLeastOnce()).updatePoint(eq(userId), any(PointUpdateDTO.class));
    }


}