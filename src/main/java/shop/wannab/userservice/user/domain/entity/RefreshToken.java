package shop.wannab.userservice.user.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@AllArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 14440)
public class RefreshToken {
    @Id
    private String refreshToken;
    private Long userId;
}
