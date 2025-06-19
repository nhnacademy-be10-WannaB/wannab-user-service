package shop.wannab.userservice.point.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PointPolicyUpdateDTO(@Positive long id,
                                   @NotBlank String name,
                                   @Positive @Max(100) int addRate,
                                   @Positive int addPoint) {

}
