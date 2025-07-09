package shop.wannab.userservice.point.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.userservice.point.domain.dto.PointHistoryCreateDTO;
import shop.wannab.userservice.point.domain.entity.PointHistory;
import shop.wannab.userservice.point.repository.PointHistoryRepository;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.exception.UserNotFoundException;
import shop.wannab.userservice.user.service.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PointHistoryServiceImpl implements PointHistoryService {
    private final PointHistoryRepository pointHistoryRepository;
    private final UserService userService;

    @Override
    public PointHistory createPointHistory(long userId, PointHistoryCreateDTO pointHistoryCreateDTO) {
        log.info("Service: createPointHistory");
        if (!userService.existsUser(userId)) {
            throw new UserNotFoundException();
        }
        PointHistory pointHistory = PointHistory.builder()
                .pointHistoryChange(pointHistoryCreateDTO.pointChange())
                .pointHistoryReason(pointHistoryCreateDTO.pointReason())
                .totalPoints(pointHistoryCreateDTO.leftPoints())
                .orderId(pointHistoryCreateDTO.orderId())
                .user(userService.readUser(userId))
                .build();

        return pointHistoryRepository.save(pointHistory);
    }

    @Override
    // 후에 페이지로 바꾸겠습니다.
    public List<PointHistory> readPointHistories(long userId) {
        log.info("Service: readPointHistories");
        User user = userService.readUser(userId);
        return pointHistoryRepository.findAllByUser(user);
    }
}
