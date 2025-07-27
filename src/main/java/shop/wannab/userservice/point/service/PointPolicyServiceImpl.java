package shop.wannab.userservice.point.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.userservice.point.domain.dto.request.PointPolicyCreateRequest;
import shop.wannab.userservice.point.domain.dto.request.PointPolicyUpdateDTO;
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
        log.info("action=updatePointPolicy, userRole={}, policyName=\"{}\", message=\"포인트 정책 업데이트 서비스 시작\"", userRole, pointPolicyUpdateDTO.name());
        if (userRole != Role.ADMIN) {
            throw new AccessDeniedException();
        }
        log.debug("action=updatePointPolicy, userRole={}, message=\"사용자 권한 검증 완료\"", userRole);

        PointPolicy pointPolicy = pointPolicyRepository.findByPolicyName(pointPolicyUpdateDTO.name()).
                orElseThrow(() -> new PointPolicyNotFoundException("해당 하는 포인트 정책 없음"));
        log.debug("action=updatePointPolicy, policyName=\"{}\", message=\"기존 포인트 정책 조회 완료\"", pointPolicyUpdateDTO.name());

        pointPolicy.setAddRate(pointPolicyUpdateDTO.addRate());
        pointPolicy.setAddPoint(pointPolicyUpdateDTO.addPoint());
        pointPolicy.setActive(pointPolicyUpdateDTO.active());
        log.info("action=updatePointPolicy, policyName=\"{}\", addRate={}, addPoint={}, message=\"포인트 정책 필드 업데이트\"", pointPolicy.getPolicyName(), pointPolicy.getAddRate(), pointPolicy.getAddPoint());

        log.info("action=updatePointPolicy, policyName=\"{}\", message=\"포인트 정책 업데이트 서비스 완료\"", pointPolicy.getPolicyName());
        return pointPolicy;
    }

    @Override
    public PointPolicy createPointPolicy(Role userRole, PointPolicyCreateRequest pointPolicyCreateRequest) {
        log.info("action=createPointPolicy, userRole={}, policyName=\"{}\", message=\"포인트 정책 생성 서비스 시작\"", userRole, pointPolicyCreateRequest.name());

        if (userRole != Role.ADMIN) {
            throw new AccessDeniedException();
        }
        log.debug("action=createPointPolicy, userRole={}, message=\"사용자 권한 검증 완료\"", userRole);

        if (pointPolicyRepository.findByPolicyName((pointPolicyCreateRequest.name())).isPresent()) {
            throw new PointPolicyAlreadyExistsException();
        }
        log.debug("action=createPointPolicy, policyName=\"{}\", message=\"정책 이름 중복 확인 완료\"", pointPolicyCreateRequest.name());

        log.info("action=createPointPolicy, policyName=\"{}\", message=\"새 포인트 정책 생성 및 저장 완료\"", pointPolicyCreateRequest.name());

        return pointPolicyRepository.save(PointPolicy.builder()
                .policyName(pointPolicyCreateRequest.name())
                .addPoint(pointPolicyCreateRequest.addPoint())
                .addRate(pointPolicyCreateRequest.addRate())
                .build()
        );
    }

    @Override
    public List<PointPolicy> readPointPolicies(Role userRole) {
        log.info("action=readPointPolicies, userRole={}, message=\"포인트 정책 목록 조회 서비스 시작\"", userRole);
        if (userRole != Role.ADMIN) {
            throw new AccessDeniedException();
        }
        log.debug("action=readPointPolicies, userRole={}, message=\"사용자 권한 검증 완료\"", userRole);

        List<PointPolicy> policies = pointPolicyRepository.findAll();

        log.info("action=readPointPolicies, userRole={}, policyCount={}, message=\"포인트 정책 목록 조회 서비스 완료\"", userRole, policies.size());

        return policies;
    }
}
