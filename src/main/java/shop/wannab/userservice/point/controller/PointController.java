package shop.wannab.userservice.point.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.userservice.point.domain.dto.PointHistoryCreateDTO;
import shop.wannab.userservice.point.domain.dto.PointPolicyUpdateDTO;
import shop.wannab.userservice.point.domain.dto.PointUpdateDTO;
import shop.wannab.userservice.point.domain.entity.PointHistory;
import shop.wannab.userservice.point.domain.entity.PointPolicy;
import shop.wannab.userservice.point.service.PointHistoryService;
import shop.wannab.userservice.point.service.PointPolicyService;
import shop.wannab.userservice.user.exception.UserIdMismatchException;
import shop.wannab.userservice.user.service.UserService;
import shop.wannab.userservice.utils.Util;

@RestController
@RequiredArgsConstructor
public class PointController {
    private final UserService userService;
    private final PointHistoryService pointHistoryService;
    private final PointPolicyService pointPolicyService;

    @GetMapping("/api/users/{user-id}/points")
    public ResponseEntity<Integer> readPoints(@PathVariable(name = "user-id") String userId,
                                              @RequestHeader(Util.HEADER_ID_NAME) String headerUserId) {
        if (!userId.equals(headerUserId)) {
            throw new UserIdMismatchException("요청자 id와 대상 id가 일치하지 않습니다");
        }
        int points = userService.readPoint(Long.parseLong(userId));
        return ResponseEntity.ok(points);
    }

    @PostMapping("/api/users/{user-id}/points")
    public ResponseEntity<Void> updatePoint(@PathVariable(name = "user-id") String userId,
                                            @RequestHeader(Util.HEADER_ID_NAME) String headerUserId,
                                            @RequestBody @Valid PointUpdateDTO pointUpdateDTO) {
        if (!userId.equals(headerUserId)) {
            throw new UserIdMismatchException("요청자 id와 대상 id가 일치하지 않습니다");
        }
        userService.updatePoint(Long.parseLong(userId), pointUpdateDTO);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/users/{user-id}/points-histories")
    public ResponseEntity<PointHistory> createPointHistory(@PathVariable(name = "user-id") String userId,
                                                           @RequestHeader(Util.HEADER_ID_NAME) String headerUserId,
                                                           @RequestBody @Valid PointHistoryCreateDTO pointHistoryCreateDTO) {
        if (!userId.equals(headerUserId)) {
            throw new UserIdMismatchException("요청자 id와 대상 id가 일치하지 않습니다");
        }
        PointHistory pointHistory = pointHistoryService.createPointHistory(Long.parseLong(userId),
                pointHistoryCreateDTO);
        URI uri = URI.create("/api/users/" + userId + "/point-histories");
        return ResponseEntity.created(uri).body(pointHistory);
    }

    @GetMapping("/api/users/{user-id}/point-histories")
    public ResponseEntity<List<PointHistory>> readPointHistory(@PathVariable(name = "user-id") String userId,
                                                               @RequestHeader(Util.HEADER_ID_NAME) String headerUserId) {
        if (!userId.equals(headerUserId)) {
            throw new UserIdMismatchException("요청자 id와 대상 id가 일치하지 않습니다");
        }
        List<PointHistory> pointHistories = pointHistoryService.readPointHistories(Long.parseLong(userId));
        return ResponseEntity.ok(pointHistories);
    }

    @PutMapping("/api/reward-rates/{reward-rate-id}")
    public ResponseEntity<PointPolicy> updatePointPolicy(@PathVariable(name = "reward-rate-id") String rewardRateId,
                                                         @RequestBody @Valid PointPolicyUpdateDTO pointPolicyUpdateDTO) {
        PointPolicy pointPolicy = pointPolicyService.updatePointPolicy(Long.parseLong(rewardRateId),
                pointPolicyUpdateDTO);
        return ResponseEntity.ok(pointPolicy);
    }
}
