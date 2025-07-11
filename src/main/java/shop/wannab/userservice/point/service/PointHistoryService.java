package shop.wannab.userservice.point.service;

import org.springframework.data.domain.Page;
import shop.wannab.userservice.point.domain.dto.PointHistoryCreateDTO;
import shop.wannab.userservice.point.domain.dto.PointHistoryResponse;
import shop.wannab.userservice.point.domain.entity.PointHistory;

public interface PointHistoryService {
    PointHistory createPointHistory(long userId, PointHistoryCreateDTO pointHistoryCreateDTO);

    Page<PointHistoryResponse> readPointHistories(long userId, int page, int size);
}
