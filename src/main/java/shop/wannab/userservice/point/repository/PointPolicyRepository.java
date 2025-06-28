package shop.wannab.userservice.point.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.userservice.point.domain.entity.PointPolicy;

public interface PointPolicyRepository extends JpaRepository<PointPolicy, Long> {
    Optional<PointPolicy> findByPolicyName(String policyName);
}
