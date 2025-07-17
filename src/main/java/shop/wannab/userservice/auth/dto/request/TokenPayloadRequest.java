package shop.wannab.userservice.auth.dto.request;

import jakarta.validation.constraints.NotNull;

public record TokenPayloadRequest(@NotNull String token) {
}
