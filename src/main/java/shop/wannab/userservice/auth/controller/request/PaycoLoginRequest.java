package shop.wannab.userservice.auth.controller.request;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record PaycoLoginRequest(@NotBlank String providerId,
                                @NotBlank String providerName,
                                String email,
                                LocalDate birthday,
                                String phone,
                                String name) {
}
