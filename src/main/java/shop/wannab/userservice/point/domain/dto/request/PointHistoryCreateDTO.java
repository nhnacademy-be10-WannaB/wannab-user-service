package shop.wannab.userservice.point.domain.dto.request;


import jakarta.validation.constraints.Positive;

public record PointHistoryCreateDTO(Long userId,
                                    int usedPoints,
                                    @Positive int orderTotalPrice,
                                    @Positive Long orderId) {
}