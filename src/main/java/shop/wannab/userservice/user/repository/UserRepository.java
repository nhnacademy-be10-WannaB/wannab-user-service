package shop.wannab.userservice.user.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.userservice.user.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long>, UserQueryDslRepository {
    Boolean existsByUserLoginId(String userLoginId);

    Optional<User> findByUserLoginId(String userLoginId);

    Optional<User> findByProviderId(String providerId);

    boolean existsByProviderId(String providerId);

    boolean existsByUserId(Long userId);
}
