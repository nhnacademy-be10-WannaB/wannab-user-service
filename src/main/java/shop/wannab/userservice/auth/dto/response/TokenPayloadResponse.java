package shop.wannab.userservice.auth.dto.response;

import java.util.Map;

public record TokenPayloadResponse(Map<String, Object> claims) {
}
