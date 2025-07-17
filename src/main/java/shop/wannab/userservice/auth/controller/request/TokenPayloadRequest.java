package shop.wannab.userservice.auth.controller.request;

import jakarta.validation.constraints.NotNull;

public record TokenPayloadRequest(@NotNull String token) {
}
