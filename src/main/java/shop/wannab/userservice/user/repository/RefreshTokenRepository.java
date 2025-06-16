package shop.wannab.userservice.user.repository;

import org.springframework.data.repository.CrudRepository;
import shop.wannab.userservice.user.domain.entity.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
