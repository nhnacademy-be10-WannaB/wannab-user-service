package shop.wannab.userservice.user.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record UserCreateDTO(@NotBlank String username,
                            // 비밀번호는 8~16자리, 특수문자, 숫자, 영어문자를 1글자 이상 포함해야 함
                            @NotBlank @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,16}$") String password,
                            @NotBlank String name,
                            @NotBlank @Email String email,
                            // 전화번호는 11자리
                            @NotBlank @Pattern(regexp = "^\\d{11}$") String phone,
                            @NotNull @Past LocalDate birth) {
}
