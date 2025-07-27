package shop.wannab.userservice.point.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.userservice.point.domain.dto.response.PageResponse;
import shop.wannab.userservice.point.domain.dto.response.PointHistoryResponse;
import shop.wannab.userservice.point.domain.dto.request.PointPolicyCreateRequest;
import shop.wannab.userservice.point.domain.dto.request.PointPolicyUpdateDTO;
import shop.wannab.userservice.point.domain.dto.request.PointUpdateDTO;
import shop.wannab.userservice.point.domain.entity.PointPolicy;
import shop.wannab.userservice.point.service.PointHistoryService;
import shop.wannab.userservice.point.service.PointPolicyService;
import shop.wannab.userservice.user.domain.entity.Role;
import shop.wannab.userservice.user.service.UserService;
import shop.wannab.userservice.utils.HeaderUtil;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PointController {
    private final UserService userService;
    private final PointHistoryService pointHistoryService;
    private final PointPolicyService pointPolicyService;

    /**
     * 회원 포인트 조회
     *
     * @param userId
     */
    @GetMapping("/api/users/points")
    public ResponseEntity<Integer> readPoints(@RequestHeader(HeaderUtil.HEADER_ID_NAME) Long userId) {
        int points = userService.readPoint(userId);
        log.info("action=readPoints, userId={}, points={}, message=\"회원 포인트 조회 완료\"", userId, points);
        return ResponseEntity.ok(points);
    }

    /**
     * 회원 포인트 수정
     *
     * @param userId
     * @param pointUpdateDTO
     */
    @PostMapping("/api/users/points")
    public ResponseEntity<Void> updatePoint(@RequestHeader(HeaderUtil.HEADER_ID_NAME) Long userId,
                                            @RequestBody @Valid PointUpdateDTO pointUpdateDTO) {
        userService.updatePoint(userId, pointUpdateDTO);
        log.info("action=updatePoint, userId={}, message=\"회원 포인트 수정 완료\"", userId);
        return ResponseEntity.noContent().build();
    }


    /**
     * 포인트 내역 조회
     *
     * @param userId
     */
    @GetMapping("/api/users/point-histories")
    public ResponseEntity<PageResponse<PointHistoryResponse>> readPointHistory(
            @RequestHeader(HeaderUtil.HEADER_ID_NAME) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PointHistoryResponse> pointHistories = pointHistoryService.readPointHistories(userId, page, size);
        log.info("action=readPointHistory, userId={}, page={}, size={}, totalElements={}, message=\"포인트 내역 조회 완료\"", userId, page, size, pointHistories.getTotalElements());
        return ResponseEntity.ok(PageResponse.from(pointHistories));
    }

    /**
     * 결제 취소 - 포인트 반환
     *
     * @param orderId
     */
    @PostMapping("/api/users/points/orders/{order-id}/cancel")
    public void cancleOrderPointProcess(@PathVariable("order-id") Long orderId) {
        pointHistoryService.cancel(orderId);
        log.info("action=cancleOrderPointProcess, orderId={}, message=\"결제 취소 - 포인트 반환 처리 완료\"", orderId);
    }

    /**
     * 환불 - 포인트로 환불, 적립된 포인트 차감
     *
     * @param orderId
     * @param refundPoint
     */
    @PostMapping("/api/users/points/refund")
    public void refundPoint(@RequestParam("order-id") Long orderId,
                            @RequestParam("amount") int refundPoint) {
        pointHistoryService.refund(orderId, refundPoint);
        log.info("action=refundPoint, orderId={}, refundAmount={}, message=\"환불 - 포인트 환불 및 적립 포인트 차감 처리 완료\"", orderId, refundPoint);
    }

    /**
     * 포인트 정책 수정
     *
     * @param userRole
     * @param pointPolicyUpdateDTO
     */
    @PutMapping("/api/reward-rates")
    public ResponseEntity<PointPolicy> updatePointPolicy(@RequestHeader(HeaderUtil.HEADER_ROLE_NAME) Role userRole,
                                                         @RequestBody @Valid PointPolicyUpdateDTO pointPolicyUpdateDTO) {
        PointPolicy pointPolicy = pointPolicyService.updatePointPolicy(userRole, pointPolicyUpdateDTO);
        log.info("action=updatePointPolicy, userRole={}, policyId={}, message=\"포인트 정책 수정 완료\"", userRole, pointPolicy.getId());
        return ResponseEntity.ok(pointPolicy);
    }

    /**
     * 포인트 정책 생성
     *
     * @param userRole
     * @param pointPolicyCreateRequest
     */
    @PostMapping("/api/reward-rates")
    public ResponseEntity<PointPolicy> createPointPolicy(@RequestHeader(HeaderUtil.HEADER_ROLE_NAME) Role userRole,
                                                         @RequestBody PointPolicyCreateRequest pointPolicyCreateRequest) {
        PointPolicy pointPolicy = pointPolicyService.createPointPolicy(userRole, pointPolicyCreateRequest);
        URI uri = URI.create("/api/reward-rates");
        log.info("action=createPointPolicy, userRole={}, policyId={}, message=\"포인트 정책 생성 완료\"", userRole, pointPolicy.getId());
        return ResponseEntity.created(uri).body(pointPolicy);
    }

    /**
     * 포인트 정책 조회
     *
     * @param userRole
     * @return List
     */
    @GetMapping("/api/reward-rates")
    public ResponseEntity<List<PointPolicy>> readPointPolicy(
            @RequestHeader(HeaderUtil.HEADER_ROLE_NAME) Role userRole) {
        List<PointPolicy> pointPolicyList = pointPolicyService.readPointPolicies(userRole);
        log.info("action=readPointPolicy, userRole={}, policyCount={}, message=\"포인트 정책 조회 완료\"", userRole, pointPolicyList.size());
        return ResponseEntity.ok(pointPolicyList);
    }

    /**
     * 리뷰 작성 시 포인트적립 및 내역생성
     */
    @PostMapping("/api/points/reviews")
    public ResponseEntity<Void> createReviewPoints(@RequestParam Long userId) {
        pointHistoryService.createReviewPoints(userId);
        log.info("action=createReviewPoints, userId={}, message=\"리뷰 작성 시 포인트 적립 및 내역 생성 완료\"", userId);
        return ResponseEntity.ok().build();
    }

}
