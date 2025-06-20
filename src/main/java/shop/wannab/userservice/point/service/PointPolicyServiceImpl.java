package shop.wannab.userservice.point.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.userservice.point.domain.dto.PointPolicyUpdateDTO;
import shop.wannab.userservice.point.domain.entity.PointPolicy;
import shop.wannab.userservice.point.exception.PointPolicyNotFoundException;
import shop.wannab.userservice.point.repository.PointPolicyRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class PointPolicyServiceImpl implements PointPolicyService {
    private final PointPolicyRepository pointPolicyRepository;

    @Override
    public PointPolicy updatePointPolicy(Long rewardRateId, PointPolicyUpdateDTO pointPolicyUpdateDTO) {
        PointPolicy pointPolicy = pointPolicyRepository.findById(rewardRateId).
                orElseThrow(() -> new PointPolicyNotFoundException("해당 하는 포인트 정책 없음"));

        pointPolicy.setPolicyName(pointPolicyUpdateDTO.name());
        pointPolicy.setAddRate(pointPolicyUpdateDTO.addRate());
        pointPolicy.setAddPoint(pointPolicyUpdateDTO.addPoint());

        return pointPolicy;
    }
}
