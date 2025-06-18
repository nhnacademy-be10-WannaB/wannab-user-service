package shop.wannab.userservice.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.userservice.user.domain.entity.UserGrade;

public interface UserGradeRepository extends JpaRepository<UserGrade, Long> {
    UserGrade findByGradeName(String gradeName);
}

