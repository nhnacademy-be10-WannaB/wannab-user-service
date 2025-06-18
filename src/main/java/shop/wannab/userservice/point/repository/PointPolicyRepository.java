package shop.wannab.userservice.point.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.userservice.point.domain.entity.PointPolicy;

public interface PointPolicyRepository extends JpaRepository<PointPolicy, Long> {
}
