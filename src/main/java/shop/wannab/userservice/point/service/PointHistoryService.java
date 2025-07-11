package shop.wannab.userservice.point.service;

import java.util.List;
import shop.wannab.userservice.point.domain.dto.PointHistoryCreateDTO;
import shop.wannab.userservice.point.domain.dto.PointHistoryRollbackPointDTO;
import shop.wannab.userservice.point.domain.entity.PointHistory;

public interface PointHistoryService {
    PointHistory createPointHistory(PointHistoryCreateDTO pointHistoryCreateDTO);

    List<PointHistory> readPointHistories(long userId);

    void rollbackPointHistory(PointHistoryRollbackPointDTO pointHistoryRollbackPointDTO);

    void cancel(Long orderId);

    void refund(Long orderId, int amount);
}
