package shop.wannab.userservice.point.domain.dto;


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

    public PointHistoryResponse(PointHistory ph) {
        this.pointsHistoryId = ph.getPointsHistoryId();
        this.pointHistoryReason = ph.getPointHistoryReason();
        this.pointHistoryChange = ph.getPointHistoryChange();
        this.totalPoints = ph.getTotalPoints();
        this.createdAt = ph.getCreatedAt();
    }
}