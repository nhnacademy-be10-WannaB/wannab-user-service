package shop.wannab.userservice.point.domain.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PointHistoryCreateDTO(@NotBlank String userId,
                                    // 증감액
                                    int pointChange,
                                    // 증감 사유
                                    @NotBlank String pointReason,
                                    // 증감 후 잔여 포인트
                                    @Positive int leftPoints,
                                    @Positive long orderId) {
}