package shop.wannab.userservice.user.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.userservice.user.domain.entity.UserGrade;

public interface UserGradeRepository extends JpaRepository<UserGrade, Long> {
    Optional<UserGrade> findByGradeName(String gradeName);
}

