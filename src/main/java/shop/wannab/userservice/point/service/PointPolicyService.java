package shop.wannab.userservice.point.service;

import shop.wannab.userservice.point.domain.dto.PointPolicyUpdateDTO;
import shop.wannab.userservice.point.domain.entity.PointPolicy;

public interface PointPolicyService {
    PointPolicy updatePointPolicy(Long rewardRateId, PointPolicyUpdateDTO pointPolicyUpdateDTO);
}
