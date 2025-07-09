package shop.wannab.userservice.point.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.userservice.point.domain.dto.PointPolicyCreateRequest;
import shop.wannab.userservice.point.domain.dto.PointPolicyUpdateDTO;
import shop.wannab.userservice.point.domain.entity.PointPolicy;
import shop.wannab.userservice.point.exception.AccessDeniedException;
import shop.wannab.userservice.point.exception.PointPolicyAlreadyExistsException;
import shop.wannab.userservice.point.exception.PointPolicyNotFoundException;
import shop.wannab.userservice.point.repository.PointPolicyRepository;
import shop.wannab.userservice.user.domain.entity.Role;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PointPolicyServiceImpl implements PointPolicyService {
    private final PointPolicyRepository pointPolicyRepository;

    @Override
    public PointPolicy updatePointPolicy(Role userRole,
                                         PointPolicyUpdateDTO pointPolicyUpdateDTO) {
        log.info("Service: updatePointPolicy");
        if (userRole != Role.ADMIN) {
            throw new AccessDeniedException();
        }
        PointPolicy pointPolicy = pointPolicyRepository.findByPolicyName(pointPolicyUpdateDTO.name()).
                orElseThrow(() -> new PointPolicyNotFoundException("해당 하는 포인트 정책 없음"));

        pointPolicy.setAddRate(pointPolicyUpdateDTO.addRate());
        pointPolicy.setAddPoint(pointPolicyUpdateDTO.addPoint());

        return pointPolicy;
    }

    @Override
    public PointPolicy createPointPolicy(Role userRole, PointPolicyCreateRequest pointPolicyCreateRequest) {
        log.info("Service: createPointPolicy");
        if (userRole != Role.ADMIN) {
            throw new AccessDeniedException();
        }
        if (pointPolicyRepository.findByPolicyName((pointPolicyCreateRequest.name())).isPresent()) {
            throw new PointPolicyAlreadyExistsException();
        }

        return pointPolicyRepository.save(PointPolicy.builder()
                .policyName(pointPolicyCreateRequest.name())
                .addPoint(pointPolicyCreateRequest.addPoint())
                .addRate(pointPolicyCreateRequest.addRate())
                .build()
        );
    }

    @Override
    public List<PointPolicy> readPointPolicies(Role userRole) {
        log.info("Service: readPointPolicies");
        if (userRole != Role.ADMIN) {
            throw new AccessDeniedException();
        }
        return pointPolicyRepository.findAll();
    }
}
