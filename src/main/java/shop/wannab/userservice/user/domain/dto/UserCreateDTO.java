package shop.wannab.userservice.user.domain.dto;

import java.time.LocalDate;

public record UserCreateDTO(String username,
                            String password,
                            String name,
                            String email,
                            String phone,
                            LocalDate birth) {
}
