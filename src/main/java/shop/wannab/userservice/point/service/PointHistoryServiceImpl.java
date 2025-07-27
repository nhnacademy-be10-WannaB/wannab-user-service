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
        log.info("action=createPointHistory, userId={}, orderId={}, usedPoints={}, orderTotalPrice={}, message=\"포인트 내역 생성 서비스 시작 (결제)\"",
                pointHistoryCreateDTO.userId(), pointHistoryCreateDTO.orderId(), pointHistoryCreateDTO.usedPoints(), pointHistoryCreateDTO.orderTotalPrice());
        if (pointHistoryCreateDTO.usedPoints() > 0) {
            log.debug("action=createPointHistory, orderId={}, usedPoints={}, message=\"포인트 사용 처리 시작\"", pointHistoryCreateDTO.orderId(), pointHistoryCreateDTO.usedPoints());
            User user = userService.readUser(pointHistoryCreateDTO.userId());
            int totalPoints = user.getPoints() - pointHistoryCreateDTO.usedPoints();
            pointExists(totalPoints);
            log.debug("action=createPointHistory, userId={}, currentPoints={}, usedPoints={}, totalPointsAfterUse={}, message=\"사용 후 포인트 잔액 계산 완료\"",
                    user.getUserId(), user.getPoints(), pointHistoryCreateDTO.usedPoints(), totalPoints);

            PointHistory pointHistory = PointHistory.builder().
                    user(user)
                    .pointHistoryReason("도서구매 포인트 사용")
                    .pointHistoryChange(-1 * pointHistoryCreateDTO.usedPoints())
                    .totalPoints(totalPoints)
                    .orderId(pointHistoryCreateDTO.orderId())
                    .build();
            pointHistoryRepository.save(pointHistory);
            userService.updatePoint(pointHistory.getUser().getUserId(), new PointUpdateDTO(totalPoints));
            log.info("action=createPointHistory, orderId={}, usedPoints={}, message=\"포인트 사용 내역 및 사용자 포인트 업데이트 완료\"", pointHistoryCreateDTO.orderId(), pointHistoryCreateDTO.usedPoints());

        }
        User user = userService.readUser(pointHistoryCreateDTO.userId());
        double rewardRates = user.getUserGrade().getRewardRate();
        int changePoints = (int) (pointHistoryCreateDTO.orderTotalPrice() * rewardRates);
        int totalPoints = user.getPoints() + changePoints;

        log.debug("action=createPointHistory, userId={}, rewardRates={}, orderTotalPrice={}, initialEarnedPoints={}, message=\"등급 기반 적립 포인트 계산\"",
                user.getUserId(), rewardRates, pointHistoryCreateDTO.orderTotalPrice(), changePoints);

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
        log.info("action=createPointHistory, userId={}, orderId={}, earnedPoints={}, finalTotalPoints={}, message=\"포인트 적립 내역 및 사용자 포인트 업데이트 완료 (결제)\"",
                pointHistoryCreateDTO.userId(), pointHistoryCreateDTO.orderId(), changePoints, totalPoints);
        return returnPointHistory;
    }

    @Override
    // 후에 페이지로 바꾸겠습니다.
    public Page<PointHistoryResponse> readPointHistories(long userId, int page, int size) {
        log.info("action=readPointHistories, userId={}, page={}, size={}, message=\"포인트 내역 조회 서비스 시작\"", userId, page, size);
        User user = userService.readUser(userId);
        log.debug("action=readPointHistories, userId={}, message=\"사용자 정보 조회 완료\"", userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PointHistory> pointHistories = pointHistoryRepository.findAllByUser(user, pageable);
        log.info("action=readPointHistories, userId={}, page={}, size={}, message=\"포인트 내역 조회 서비스 완료\"",
                userId, page, size);
        return pointHistories.map(PointHistoryResponse::new);
    }

    /**
     * 주문 취소에 대한 포인트 내역 생성
     *
     * @param pointHistoryRollbackPointDTO
     */
    @Override
    public void rollbackPointHistory(PointHistoryRollbackPointDTO pointHistoryRollbackPointDTO) {
        log.info("action=rollbackPointHistory, orderId={}, userId={}, change={}, reason={}, message=\"포인트 내역 롤백 처리 시작\"",
                pointHistoryRollbackPointDTO.orderId(), pointHistoryRollbackPointDTO.user().getUserId(), pointHistoryRollbackPointDTO.pointHistoryChange(), pointHistoryRollbackPointDTO.pointHistoryReason());

        PointHistory pointHistory = PointHistory.builder()
                .pointHistoryChange(pointHistoryRollbackPointDTO.pointHistoryChange())
                .pointHistoryReason(pointHistoryRollbackPointDTO.pointHistoryReason())
                .totalPoints(pointHistoryRollbackPointDTO.totalPoints())
                .orderId(pointHistoryRollbackPointDTO.orderId())
                .user(pointHistoryRollbackPointDTO.user())
                .build();
        pointHistoryRepository.save(pointHistory);

        log.info("action=rollbackPointHistory, orderId={}, userId={}, change={}, message=\"포인트 내역 롤백 처리 완료\"",
                pointHistoryRollbackPointDTO.orderId(), pointHistoryRollbackPointDTO.user().getUserId(), pointHistoryRollbackPointDTO.pointHistoryChange());

    }

    /**
     * 주문 취소
     *
     * @param orderId
     */
    @Override
    public void cancel(Long orderId) {
        log.info("action=cancel, orderId={}, message=\"주문 취소로 인한 포인트 처리 시작\"", orderId);

        List<PointHistory> pointHistories = pointHistoryRepository.findPointHistoriesByOrderId(orderId);
        log.debug("action=cancel, orderId={}, historyCount={}, message=\"주문 관련 포인트 내역 조회 완료\"", orderId, pointHistories.size());

        for (PointHistory pointHistory : pointHistories) {
            if (pointHistory.getPointHistoryReason().equals("도서구매")) {
                log.debug("action=cancel, orderId={}, userId={}, reason=\"도서구매\", message=\"도서구매 적립 포인트 회수 처리\"", orderId, pointHistory.getUser().getUserId());

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
                log.info("action=cancel, orderId={}, userId={}, change={}, message=\"도서구매 적립 포인트 회수 및 사용자 포인트 업데이트 완료\"", orderId, pointHistory.getUser().getUserId(), -1 * pointHistory.getPointHistoryChange());

            }
            if (pointHistory.getPointHistoryReason().equals("도서구매 포인트 사용")) {
                log.debug("action=cancel, orderId={}, userId={}, reason=\"도서구매 포인트 사용\", message=\"도서구매 사용 포인트 복원 처리\"", orderId, pointHistory.getUser().getUserId());

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
                log.info("action=cancel, orderId={}, userId={}, change={}, message=\"도서구매 사용 포인트 복원 및 사용자 포인트 업데이트 완료\"",
                        orderId, pointHistory.getUser().getUserId(), pointHistory.getPointHistoryChange());

            }
        }
        log.info("action=cancel, orderId={}, message=\"주문 취소로 인한 포인트 처리 최종 완료\"", orderId);

    }

    @Override
    public void refund(Long orderId, int amount) {
        log.info("action=refund, orderId={}, refundAmount={}, message=\"포인트 환불 처리 시작\"", orderId, amount);

        List<PointHistory> pointHistories = pointHistoryRepository.findPointHistoriesByOrderId(orderId);
        log.debug("action=refund, orderId={}, historyCount={}, message=\"환불 관련 포인트 내역 조회 완료\"", orderId, pointHistories.size());

        for (PointHistory pointHistory : pointHistories) {
            if (pointHistory.getPointHistoryReason().equals("도서구매")) {
                log.debug("action=refund, orderId={}, userId={}, reason=\"도서구매\", message=\"도서구매 환불 포인트 적립 처리\"", orderId, pointHistory.getUser().getUserId());
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
                log.info("action=refund, orderId={}, userId={}, refundAmount={}, message=\"도서구매 환불 포인트 적립 및 사용자 포인트 업데이트 완료\"", orderId, user.getUserId(), amount);

            }
        }
        log.info("action=refund, orderId={}, message=\"환불 처리 완료, 추가로 주문 취소 로직 호출\"", orderId);
        cancel(orderId);
        log.info("action=refund, orderId={}, message=\"포인트 환불 처리 최종 완료\"", orderId);

    }

    @Override
    public void createReviewPoints(Long userId) {
        log.info("action=createReviewPoints, userId={}, message=\"리뷰 작성 포인트 적립 서비스 시작\"", userId);
        User user = userService.readUser(userId);
        log.debug("action=createReviewPoints, userId={}, message=\"사용자 정보 조회 완료\"", userId);

        PointPolicy policy = pointPolicyRepository.findByPolicyName("리뷰작성").orElse(null);
        if (policy != null) {
            int point = policy.getAddPoint();
            int totalPoints = user.getPoints() + point;
            log.debug("action=createReviewPoints, userId={}, reviewPoints={}, totalPointsAfterEarn={}, message=\"리뷰 작성 포인트 계산 완료\"", userId, point, totalPoints);

            PointHistory pointHistory = PointHistory.builder()
                    .orderId(userId)
                    .user(user)
                    .pointHistoryChange(point)
                    .pointHistoryReason("리뷰작성")
                    .totalPoints(totalPoints)
                    .build();
            pointHistoryRepository.save(pointHistory);
            userService.updatePoint(user.getUserId(), new PointUpdateDTO(totalPoints));
            log.info("action=createReviewPoints, userId={}, earnedPoints={}, message=\"리뷰 작성 포인트 적립 및 내역 생성 완료\"", userId, point);

        }
        log.info("action=createReviewPoints, userId={}, message=\"리뷰 작성 포인트 적립 서비스 최종 완료\"", userId);


    }

    @Override
    public void createSignupPoints(User user) {
        log.info("action=createSignupPoints, userId={}, message=\"회원가입 포인트 적립 서비스 시작\"", user.getUserId());

        PointPolicy policy = pointPolicyRepository.findByPolicyName("회원가입").orElse(null);
        if (policy != null) {
            int point = policy.getAddPoint();
            int totalPoints = user.getPoints() + point;
            log.debug("action=createSignupPoints, userId={}, signupPoints={}, totalPointsAfterEarn={}, message=\"회원가입 포인트 계산 완료\"", user.getUserId(), point, totalPoints);

            PointHistory pointHistory = PointHistory.builder()
                    .user(user)
                    .pointHistoryChange(point)
                    .pointHistoryReason("회원가입")
                    .totalPoints(totalPoints)
                    .build();
            pointHistoryRepository.save(pointHistory);
            userService.updatePoint(user.getUserId(), new PointUpdateDTO(totalPoints));
            log.info("action=createSignupPoints, userId={}, earnedPoints={}, message=\"회원가입 포인트 적립 및 내역 생성 완료\"", user.getUserId(), point);

        }
        log.info("action=createSignupPoints, userId={}, message=\"회원가입 포인트 적립 서비스 최종 완료\"", user.getUserId());

    }

    public void pointExists(int totalPoints) {
        if (totalPoints < 0) {
            throw new PointNotEnoughException("포인트는 음수가 될 수 없습니다.");
        }
        log.debug("action=pointExists, totalPoints={}, message=\"포인트 잔액 음수 여부 검사 완료\"", totalPoints);

    }
}
