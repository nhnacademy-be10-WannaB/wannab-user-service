package shop.wannab.userservice.point.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.userservice.point.domain.dto.request.PointHistoryCreateDTO;
import shop.wannab.userservice.point.domain.dto.request.PointHistoryRollbackPointDTO;
import shop.wannab.userservice.point.domain.dto.request.PointUpdateDTO;
import shop.wannab.userservice.point.domain.dto.response.PointHistoryResponse;
import shop.wannab.userservice.point.domain.entity.PointHistory;
import shop.wannab.userservice.point.domain.entity.PointPolicy;
import shop.wannab.userservice.point.exception.PointNotEnoughException;
import shop.wannab.userservice.point.repository.PointHistoryRepository;
import shop.wannab.userservice.point.repository.PointPolicyRepository;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.service.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PointHistoryServiceImpl implements PointHistoryService {
    private final PointHistoryRepository pointHistoryRepository;
    private final UserService userService;
    private final PointPolicyRepository pointPolicyRepository;

    /**
     * 결제에 대한 포인트 내역 생성
     */
    @Override
    public PointHistory createPointHistory(PointHistoryCreateDTO pointHistoryCreateDTO) {
        log.info("Service: createPointHistory");
        if (pointHistoryCreateDTO.usedPoints() > 0) {
            User user = userService.readUser(pointHistoryCreateDTO.userId());
            int totalPoints = user.getPoints() - pointHistoryCreateDTO.usedPoints();
            pointExists(totalPoints);
            PointHistory pointHistory = PointHistory.builder().
                    user(user)
                    .pointHistoryReason("도서구매 포인트 사용")
                    .pointHistoryChange(-1 * pointHistoryCreateDTO.usedPoints())
                    .totalPoints(totalPoints)
                    .orderId(pointHistoryCreateDTO.orderId())
                    .build();
            pointHistoryRepository.save(pointHistory);
            userService.updatePoint(pointHistory.getUser().getUserId(), new PointUpdateDTO(totalPoints));
        }
        User user = userService.readUser(pointHistoryCreateDTO.userId());
        double rewardRates = user.getUserGrade().getRewardRate();
        int changePoints = (int) (pointHistoryCreateDTO.orderTotalPrice() * rewardRates);
        int totalPoints = user.getPoints() + changePoints;
        PointPolicy policy = pointPolicyRepository.findByPolicyName("기본적립률").orElse(null);
        if (policy != null) {
            totalPoints += policy.getAddRate() * pointHistoryCreateDTO.orderTotalPrice();
            changePoints += policy.getAddRate();
        }
        PointHistory pointHistory = PointHistory.builder().
                user(user)
                .pointHistoryReason("도서구매")
                .pointHistoryChange(changePoints)
                .totalPoints(totalPoints)
                .orderId(pointHistoryCreateDTO.orderId())
                .build();
        PointHistory returnPointHistory = pointHistoryRepository.save(pointHistory);
        userService.updatePoint(pointHistory.getUser().getUserId(), new PointUpdateDTO(totalPoints));
        return returnPointHistory;
    }

    @Override
    // 후에 페이지로 바꾸겠습니다.
    public Page<PointHistoryResponse> readPointHistories(long userId, int page, int size) {
        log.info("Service: readPointHistories");
        User user = userService.readUser(userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PointHistory> pointHistories = pointHistoryRepository.findAllByUser(user, pageable);
        return pointHistories.map(PointHistoryResponse::new);
    }

    /**
     * 주문 취소에 대한 포인트 내역 생성
     *
     * @param pointHistoryRollbackPointDTO
     */
    @Override
    public void rollbackPointHistory(PointHistoryRollbackPointDTO pointHistoryRollbackPointDTO) {
        PointHistory pointHistory = PointHistory.builder()
                .pointHistoryChange(pointHistoryRollbackPointDTO.pointHistoryChange())
                .pointHistoryReason(pointHistoryRollbackPointDTO.pointHistoryReason())
                .totalPoints(pointHistoryRollbackPointDTO.totalPoints())
                .orderId(pointHistoryRollbackPointDTO.orderId())
                .user(pointHistoryRollbackPointDTO.user())
                .build();
        pointHistoryRepository.save(pointHistory);
    }

    /**
     * 주문 취소
     *
     * @param orderId
     */
    @Override
    public void cancel(Long orderId) {
        List<PointHistory> pointHistories = pointHistoryRepository.findPointHistoriesByOrderId(orderId);
        for (PointHistory pointHistory : pointHistories) {
            if (pointHistory.getPointHistoryReason().equals("도서구매")) {
                User user = userService.readUser(pointHistory.getUser().getUserId());
                int totalPoints = user.getPoints() - pointHistory.getPointHistoryChange();
                pointExists(totalPoints);
                // 회수할 포인트에 대한 포인트 내역 생성
                PointHistoryRollbackPointDTO pointHistoryRollbackPointDTO = PointHistoryRollbackPointDTO.builder()
                        .user(pointHistory.getUser())
                        .orderId(pointHistory.getOrderId())
                        .pointHistoryChange(-1 * pointHistory.getPointHistoryChange())
                        .pointHistoryReason("포인트 적립 회수")
                        .totalPoints(totalPoints)
                        .build();
                rollbackPointHistory(pointHistoryRollbackPointDTO);
                // 회원의 포인트 수정
                userService.updatePoint(user.getUserId(), new PointUpdateDTO(totalPoints));
            }
            if (pointHistory.getPointHistoryReason().equals("도서구매 포인트 사용")) {
                User user = userService.readUser(pointHistory.getUser().getUserId());
                int totalPoints = user.getPoints() + pointHistory.getPointHistoryChange();

                //  되돌려줄 포인트에 대한 포인트 내역 생성
                PointHistoryRollbackPointDTO pointHistoryRollbackPointDTO = PointHistoryRollbackPointDTO.builder()
                        .user(pointHistory.getUser())
                        .orderId(pointHistory.getOrderId())
                        .pointHistoryChange(pointHistory.getPointHistoryChange())
                        .pointHistoryReason("포인트 복원")
                        .totalPoints(totalPoints)
                        .build();
                rollbackPointHistory(pointHistoryRollbackPointDTO);
                // 회원의 포인트 수정
                userService.updatePoint(pointHistory.getUser().getUserId(), new PointUpdateDTO(totalPoints));
            }
        }
    }

    @Override
    public void refund(Long orderId, int amount) {
        List<PointHistory> pointHistories = pointHistoryRepository.findPointHistoriesByOrderId(orderId);
        for (PointHistory pointHistory : pointHistories) {
            if (pointHistory.getPointHistoryReason().equals("도서구매")) {
                User user = userService.readUser(pointHistory.getUser().getUserId());
                int totalPoints = user.getPoints() + amount;
                // 환불될 포인트 내역
                PointHistory refundHistory = PointHistory.builder()
                        .user(user)
                        .orderId(orderId)
                        .pointHistoryChange(amount)
                        .pointHistoryReason("도서구매 환불")
                        .totalPoints(totalPoints)
                        .build();
                pointHistoryRepository.save(refundHistory);

                // 포인트로 환불
                userService.updatePoint(user.getUserId(), new PointUpdateDTO(totalPoints));
            }
        }
        cancel(orderId);
    }

    @Override
    public void createReviewPoints(Long userId) {
        User user = userService.readUser(userId);
        PointPolicy policy = pointPolicyRepository.findByPolicyName("리뷰작성").orElse(null);
        if (policy != null) {
            int point = policy.getAddPoint();
            int totalPoints = user.getPoints() + point;
            PointHistory pointHistory = PointHistory.builder()
                    .orderId(userId)
                    .user(user)
                    .pointHistoryChange(point)
                    .pointHistoryReason("리뷰작성")
                    .totalPoints(totalPoints)
                    .build();
            pointHistoryRepository.save(pointHistory);
            userService.updatePoint(user.getUserId(), new PointUpdateDTO(totalPoints));
        }

    }

    @Override
    public void createSignupPoints(User user) {
        PointPolicy policy = pointPolicyRepository.findByPolicyName("회원가입").orElse(null);
        if (policy != null) {
            int point = policy.getAddPoint();
            int totalPoints = user.getPoints() + point;
            PointHistory pointHistory = PointHistory.builder()
                    .user(user)
                    .pointHistoryChange(point)
                    .pointHistoryReason("회원가입")
                    .totalPoints(totalPoints)
                    .build();
            pointHistoryRepository.save(pointHistory);
            userService.updatePoint(user.getUserId(), new PointUpdateDTO(totalPoints));
        }

    }

    public void pointExists(int totalPoints) {
        if (totalPoints < 0) {
            throw new PointNotEnoughException("포인트는 음수가 될 수 없습니다.");
        }

    }
}
