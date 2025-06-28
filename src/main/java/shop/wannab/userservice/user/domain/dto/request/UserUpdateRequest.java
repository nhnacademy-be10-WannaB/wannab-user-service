package shop.wannab.userservice.user.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserUpdateRequest(
        @NotBlank @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,16}$") String password,
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String nickname,
        @NotBlank @Pattern(regexp = "^\\d{11}$") String phone) {
}
