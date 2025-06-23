package shop.wannab.userservice.point.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record PointPolicyCreateRequest(@NotBlank String name,
                                       @PositiveOrZero @Max(100) int addRate,
                                       @PositiveOrZero int addPoint) {
}

//public record PointPolicyCreateRequest(String name,
//                                       int addRate,
//                                       int addPoint) {
//}