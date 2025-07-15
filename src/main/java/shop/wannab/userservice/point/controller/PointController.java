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
import shop.wannab.userservice.point.domain.dto.PageResponse;
import shop.wannab.userservice.point.domain.dto.PointHistoryCreateDTO;
import shop.wannab.userservice.point.domain.dto.PointHistoryResponse;
import shop.wannab.userservice.point.domain.dto.PointPolicyCreateRequest;
import shop.wannab.userservice.point.domain.dto.PointPolicyUpdateDTO;
import shop.wannab.userservice.point.domain.dto.PointUpdateDTO;
import shop.wannab.userservice.point.domain.entity.PointHistory;
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
        log.info("Controller: readPoints");
        int points = userService.readPoint(userId);
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
        log.info("Controller: updatePoint");
        userService.updatePoint(userId, pointUpdateDTO);
        return ResponseEntity.noContent().build();
    }

    /**
     * 포인트 내역 생성
     *
     * @param pointHistoryCreateDTO
     */
    @PostMapping("/api/users/points-histories")
    public ResponseEntity<PointHistory> createPointHistory(
            @RequestBody @Valid PointHistoryCreateDTO pointHistoryCreateDTO) {
        log.info("Controller: createPointHistory");

        PointHistory pointHistory = pointHistoryService.createPointHistory(pointHistoryCreateDTO);
        return ResponseEntity.ok(pointHistory);
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
        log.info("Controller: readPointHistory");
        Page<PointHistoryResponse> pointHistories = pointHistoryService.readPointHistories(userId, page, size);
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
        log.info("Controller: updatePointPolicy");
        PointPolicy pointPolicy = pointPolicyService.updatePointPolicy(userRole, pointPolicyUpdateDTO);
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
        log.info("Controller: createPointPolicy");
        PointPolicy pointPolicy = pointPolicyService.createPointPolicy(userRole, pointPolicyCreateRequest);
        URI uri = URI.create("/api/reward-rates");
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
        log.info("Controller: readPointPolicy");
        List<PointPolicy> pointPolicyList = pointPolicyService.readPointPolicies(userRole);
        return ResponseEntity.ok(pointPolicyList);
    }

}
