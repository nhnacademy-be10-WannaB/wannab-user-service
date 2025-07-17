package shop.wannab.userservice.auth.controller.response;

import java.util.Map;

public record TokenPayloadResponse(Map<String, Object> claims) {
}
