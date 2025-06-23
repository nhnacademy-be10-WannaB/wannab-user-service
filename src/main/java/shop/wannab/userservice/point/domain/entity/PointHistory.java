package shop.wannab.userservice.point.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import shop.wannab.userservice.user.domain.entity.User;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PointHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_history_id")
    private long pointsHistoryId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "order_id")
    private long orderId;
    @Column(name = "point_history_reason")
    private String pointHistoryReason;
    @Column(name = "point_history_change")
    private int pointHistoryChange;
    @Column(name = "total_points")
    private int totalPoints;
    @Builder.Default
    @Column(name = "create_at")
    private ZonedDateTime createdAt = ZonedDateTime.now();
}
