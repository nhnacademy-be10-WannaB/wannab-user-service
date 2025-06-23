package shop.wannab.userservice.point.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.userservice.point.domain.dto.PointHistoryCreateDTO;
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

@RestController
@RequiredArgsConstructor
public class PointController {
    private final UserService userService;
    private final PointHistoryService pointHistoryService;
    private final PointPolicyService pointPolicyService;

    @GetMapping("/api/users/points")
    public ResponseEntity<Integer> readPoints(@RequestHeader(Util.HEADER_ID_NAME) Long userId) {
        int points = userService.readPoint(userId);
        return ResponseEntity.ok(points);
    }

    @PostMapping("/api/users/points")
    public ResponseEntity<Void> updatePoint(@RequestHeader(Util.HEADER_ID_NAME) Long userId,
                                            @RequestBody @Valid PointUpdateDTO pointUpdateDTO) {
        userService.updatePoint(userId, pointUpdateDTO);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/users/points-histories")
    public ResponseEntity<PointHistory> createPointHistory(@RequestHeader(Util.HEADER_ID_NAME) Long userId,
                                                           @RequestBody @Valid PointHistoryCreateDTO pointHistoryCreateDTO) {

        PointHistory pointHistory = pointHistoryService.createPointHistory(userId, pointHistoryCreateDTO);
        URI uri = URI.create("/api/users/" + userId + "/point-histories");
        return ResponseEntity.created(uri).body(pointHistory);
    }

    @GetMapping("/api/users/point-histories")
    public ResponseEntity<List<PointHistory>> readPointHistory(@RequestHeader(Util.HEADER_ID_NAME) Long userId) {
        List<PointHistory> pointHistories = pointHistoryService.readPointHistories(userId);
        return ResponseEntity.ok(pointHistories);
    }

    @PutMapping("/api/reward-rates")
    public ResponseEntity<PointPolicy> updatePointPolicy(@RequestHeader(Util.HEADER_ROLE_NAME) Role userRole,
                                                         @RequestBody @Valid PointPolicyUpdateDTO pointPolicyUpdateDTO) {
        PointPolicy pointPolicy = pointPolicyService.updatePointPolicy(userRole, pointPolicyUpdateDTO);
        return ResponseEntity.ok(pointPolicy);
    }

    @PostMapping("/api/reward-rates")
    public ResponseEntity<PointPolicy> createPointPolicy(@RequestHeader(Util.HEADER_ROLE_NAME) Role userRole,
                                                         @RequestBody PointPolicyCreateRequest pointPolicyCreateRequest) {
        PointPolicy pointPolicy = pointPolicyService.createPointPolicy(userRole, pointPolicyCreateRequest);
        URI uri = URI.create("/api/reward-rates");
        return ResponseEntity.created(uri).body(pointPolicy);
    }

    @GetMapping("/api/reward-rates")
    public ResponseEntity<List<PointPolicy>> readPointPolicy(
            @RequestHeader(Util.HEADER_ROLE_NAME) Role userRole) {
        List<PointPolicy> pointPolicyList = pointPolicyService.readPointPolicies(userRole);
        return ResponseEntity.ok(pointPolicyList);
    }

}
