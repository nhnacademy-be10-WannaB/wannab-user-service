package shop.wannab.userservice.point.service;

import java.util.List;
import shop.wannab.userservice.point.domain.dto.PointPolicyCreateRequest;
import shop.wannab.userservice.point.domain.dto.PointPolicyUpdateDTO;
import shop.wannab.userservice.point.domain.entity.PointPolicy;
import shop.wannab.userservice.user.domain.entity.Role;

public interface PointPolicyService {
    PointPolicy updatePointPolicy(Role userRole, PointPolicyUpdateDTO pointPolicyUpdateDTO);

    PointPolicy createPointPolicy(Role userRole, PointPolicyCreateRequest pointPolicyCreateRequest);

    List<PointPolicy> readPointPolicies(Role userRole);
}
