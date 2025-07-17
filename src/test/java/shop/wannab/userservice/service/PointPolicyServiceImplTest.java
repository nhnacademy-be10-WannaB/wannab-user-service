package shop.wannab.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import shop.wannab.userservice.point.domain.dto.request.PointPolicyCreateRequest;
import shop.wannab.userservice.point.domain.dto.request.PointPolicyUpdateDTO;
import shop.wannab.userservice.point.domain.entity.PointPolicy;
import shop.wannab.userservice.point.exception.AccessDeniedException;
import shop.wannab.userservice.point.exception.PointPolicyAlreadyExistsException;
import shop.wannab.userservice.point.exception.PointPolicyNotFoundException;
import shop.wannab.userservice.point.repository.PointPolicyRepository;
import shop.wannab.userservice.point.service.PointPolicyServiceImpl;
import shop.wannab.userservice.user.domain.entity.Role;

class PointPolicyServiceImplTest {

    @InjectMocks
    private PointPolicyServiceImpl pointPolicyService;

    @Mock
    private PointPolicyRepository pointPolicyRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("createPointPolicy() 테스트")
    class CreatePointPolicyTest {

        @Test
        @DisplayName("성공적으로 생성된다.")
        void create_success() {
            // given
            PointPolicyCreateRequest request = new PointPolicyCreateRequest("SIGNUP", 100, 1);
            when(pointPolicyRepository.findByPolicyName("SIGNUP")).thenReturn(Optional.empty());
            when(pointPolicyRepository.save(any(PointPolicy.class)))
                    .thenReturn(PointPolicy.builder()
                            .policyName("SIGNUP")
                            .addPoint(100)
                            .addRate(1)
                            .build());

            // when
            PointPolicy result = pointPolicyService.createPointPolicy(Role.ADMIN, request);

            // then
            assertThat(result.getPolicyName()).isEqualTo("SIGNUP");
            assertThat(result.getAddPoint()).isEqualTo(100);
            assertThat(result.getAddRate()).isEqualTo(1);
        }

        @Test
        @DisplayName("이미 존재하면 예외 발생")
        void create_alreadyExists() {
            // given
            PointPolicyCreateRequest request = new PointPolicyCreateRequest("SIGNUP", 100, 1);
            when(pointPolicyRepository.findByPolicyName("SIGNUP"))
                    .thenReturn(Optional.of(new PointPolicy()));

            // when & then
            assertThatThrownBy(() ->
                    pointPolicyService.createPointPolicy(Role.ADMIN, request)
            ).isInstanceOf(PointPolicyAlreadyExistsException.class);
        }

        @Test
        @DisplayName("ADMIN이 아니면 예외 발생")
        void create_notAdmin() {
            PointPolicyCreateRequest request = new PointPolicyCreateRequest("SIGNUP", 100, 1);

            assertThatThrownBy(() ->
                    pointPolicyService.createPointPolicy(Role.USER, request)
            ).isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("updatePointPolicy() 테스트")
    class UpdatePointPolicyTest {

        @Test
        @DisplayName("정상적으로 수정된다.")
        void update_success() {
            // given
            PointPolicyUpdateDTO dto = new PointPolicyUpdateDTO("SIGNUP", 2, 150, true);
            PointPolicy policy = PointPolicy.builder()
                    .policyName("SIGNUP")
                    .addPoint(0)
                    .addRate(0)
                    .active(false)
                    .build();

            when(pointPolicyRepository.findByPolicyName("SIGNUP"))
                    .thenReturn(Optional.of(policy));

            // when
            PointPolicy result = pointPolicyService.updatePointPolicy(Role.ADMIN, dto);

            // then
            assertThat(result.getAddPoint()).isEqualTo(150);
            assertThat(result.getAddRate()).isEqualTo(2);
            assertThat(result.getActive()).isTrue();
        }


        @Test
        @DisplayName("정책이 없으면 예외 발생")
        void update_notFound() {
            PointPolicyUpdateDTO dto = new PointPolicyUpdateDTO("UNKNOWN", 100, 1, true);
            when(pointPolicyRepository.findByPolicyName("UNKNOWN"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    pointPolicyService.updatePointPolicy(Role.ADMIN, dto)
            ).isInstanceOf(PointPolicyNotFoundException.class);
        }

        @Test
        @DisplayName("ADMIN이 아니면 예외 발생")
        void update_notAdmin() {
            PointPolicyUpdateDTO dto = new PointPolicyUpdateDTO("SIGNUP", 150, 2, true);

            assertThatThrownBy(() ->
                    pointPolicyService.updatePointPolicy(Role.USER, dto)
            ).isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("readPointPolicies() 테스트")
    class ReadPointPoliciesTest {

        @Test
        @DisplayName("관리자는 모든 정책을 조회할 수 있다.")
        void read_success() {
            // given
            when(pointPolicyRepository.findAll()).thenReturn(List.of(new PointPolicy(), new PointPolicy()));

            // when
            List<PointPolicy> result = pointPolicyService.readPointPolicies(Role.ADMIN);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("ADMIN이 아니면 예외 발생")
        void read_notAdmin() {
            assertThatThrownBy(() ->
                    pointPolicyService.readPointPolicies(Role.USER)
            ).isInstanceOf(AccessDeniedException.class);
        }
    }
}
