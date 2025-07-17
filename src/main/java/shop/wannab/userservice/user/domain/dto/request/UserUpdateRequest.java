package shop.wannab.userservice.user.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserUpdateRequest(
        @NotBlank String password,
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String nickname,
        @NotBlank @Pattern(regexp = "^\\d{11}$") String phone) {
}
