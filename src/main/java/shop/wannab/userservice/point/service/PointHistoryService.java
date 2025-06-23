package shop.wannab.userservice.point.service;

import java.util.List;
import shop.wannab.userservice.point.domain.dto.PointHistoryCreateDTO;
import shop.wannab.userservice.point.domain.entity.PointHistory;

public interface PointHistoryService {
    PointHistory createPointHistory(long userId, PointHistoryCreateDTO pointHistoryCreateDTO);

    List<PointHistory> readPointHistories(long userId);
}
