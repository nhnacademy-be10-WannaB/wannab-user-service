package shop.wannab.userservice.point.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
import shop.wannab.userservice.utils.Util;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PointController {
    private final UserService userService;
    private final PointHistoryService pointHistoryService;
    private final PointPolicyService pointPolicyService;

    @GetMapping("/api/users/points")
    public ResponseEntity<Integer> readPoints(@RequestHeader(Util.HEADER_ID_NAME) Long userId) {
        log.info("Controller: readPoints");
        int points = userService.readPoint(userId);
        return ResponseEntity.ok(points);
    }

    @PostMapping("/api/users/points")
    public ResponseEntity<Void> updatePoint(@RequestHeader(Util.HEADER_ID_NAME) Long userId,
                                            @RequestBody @Valid PointUpdateDTO pointUpdateDTO) {
        log.info("Controller: updatePoint");
        userService.updatePoint(userId, pointUpdateDTO);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/users/points-histories")
    public ResponseEntity<PointHistory> createPointHistory(@RequestHeader(Util.HEADER_ID_NAME) Long userId,
                                                           @RequestBody @Valid PointHistoryCreateDTO pointHistoryCreateDTO) {
        log.info("Controller: createPointHistory");

        PointHistory pointHistory = pointHistoryService.createPointHistory(userId, pointHistoryCreateDTO);
        URI uri = URI.create("/api/users/" + userId + "/point-histories");
        return ResponseEntity.created(uri).body(pointHistory);
    }

    /**
     * 페이징 + 엔티티에서 dto로 응답하도록 수정
     *
     * @param userId
     * @return
     */
    @GetMapping("/api/users/point-histories")
    public ResponseEntity<PageResponse<PointHistoryResponse>> readPointHistory(
            @RequestHeader(Util.HEADER_ID_NAME) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Controller: readPointHistory");
        Page<PointHistoryResponse> pointHistories = pointHistoryService.readPointHistories(userId, page, size);
        return ResponseEntity.ok(PageResponse.from(pointHistories));
    }

    @PutMapping("/api/reward-rates")
    public ResponseEntity<PointPolicy> updatePointPolicy(@RequestHeader(Util.HEADER_ROLE_NAME) Role userRole,
                                                         @RequestBody @Valid PointPolicyUpdateDTO pointPolicyUpdateDTO) {
        log.info("Controller: updatePointPolicy");
        PointPolicy pointPolicy = pointPolicyService.updatePointPolicy(userRole, pointPolicyUpdateDTO);
        return ResponseEntity.ok(pointPolicy);
    }

    @PostMapping("/api/reward-rates")
    public ResponseEntity<PointPolicy> createPointPolicy(@RequestHeader(Util.HEADER_ROLE_NAME) Role userRole,
                                                         @RequestBody PointPolicyCreateRequest pointPolicyCreateRequest) {
        log.info("Controller: createPointPolicy");
        PointPolicy pointPolicy = pointPolicyService.createPointPolicy(userRole, pointPolicyCreateRequest);
        URI uri = URI.create("/api/reward-rates");
        return ResponseEntity.created(uri).body(pointPolicy);
    }

    @GetMapping("/api/reward-rates")
    public ResponseEntity<List<PointPolicy>> readPointPolicy(
            @RequestHeader(Util.HEADER_ROLE_NAME) Role userRole) {
        log.info("Controller: readPointPolicy");
        List<PointPolicy> pointPolicyList = pointPolicyService.readPointPolicies(userRole);
        return ResponseEntity.ok(pointPolicyList);
    }

}
