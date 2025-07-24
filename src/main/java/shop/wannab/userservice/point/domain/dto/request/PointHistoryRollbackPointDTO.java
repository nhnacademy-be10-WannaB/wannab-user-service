package shop.wannab.userservice.point.domain.dto.request;

import lombok.Builder;
import shop.wannab.userservice.user.domain.entity.User;

@Builder
public record PointHistoryRollbackPointDTO(
        User user,
        Long orderId,
        String pointHistoryReason,
        int pointHistoryChange,
        int totalPoints
) {
}
