package shop.wannab.userservice.user.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record UserCreateRequest(@NotBlank String userLoginId,
                                @NotBlank String password,
                                @NotBlank String name,
                                @NotBlank @Email String email,
                                // 전화번호는 11자리
                                @NotBlank @Pattern(regexp = "^\\d{11}$") String phone,
                                @NotNull @Past LocalDate birth) {
}
