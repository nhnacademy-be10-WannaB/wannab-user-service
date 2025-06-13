package shop.wannab.userservice.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.userservice.user.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
   Boolean existsByUsername(String username);


   List<User> findByUsername(String username);
}
