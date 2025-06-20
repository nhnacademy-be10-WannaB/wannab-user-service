package shop.wannab.userservice.point.repository;


import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.userservice.point.domain.entity.PointHistory;
import shop.wannab.userservice.user.domain.entity.User;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    List<PointHistory> findAllByUser(User user);
}
