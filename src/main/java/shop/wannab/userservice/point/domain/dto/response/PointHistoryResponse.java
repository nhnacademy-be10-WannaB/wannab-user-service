package shop.wannab.userservice.point.domain.dto.response;


import java.time.ZonedDateTime;
import lombok.Data;
import shop.wannab.userservice.point.domain.entity.PointHistory;

@Data
public class PointHistoryResponse {
    private Long pointsHistoryId;
    private String pointHistoryReason;
    private int pointHistoryChange;
    private int totalPoints;
    private ZonedDateTime createdAt;

    public PointHistoryResponse(PointHistory pointHistory) {
        this.pointsHistoryId = pointHistory.getPointsHistoryId();
        this.pointHistoryReason = pointHistory.getPointHistoryReason();
        this.pointHistoryChange = pointHistory.getPointHistoryChange();
        this.totalPoints = pointHistory.getTotalPoints();
        this.createdAt = pointHistory.getCreatedAt();
    }
}