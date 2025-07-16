package shop.wannab.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shop.wannab.userservice.address.domain.dto.UserAddressCreateRequest;
import shop.wannab.userservice.address.domain.dto.UserAddressResponse;
import shop.wannab.userservice.address.domain.dto.UserAddressUpdateRequest;
import shop.wannab.userservice.address.domain.entity.UserAddress;
import shop.wannab.userservice.address.exception.AlreadyExistsUserAddressException;
import shop.wannab.userservice.address.exception.UserAddressFullException;
import shop.wannab.userservice.address.exception.UserAddressNotFoundException;
import shop.wannab.userservice.address.repository.UserAddressRepository;
import shop.wannab.userservice.address.service.impl.UserAddressServiceImpl;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceImplTest {

    @InjectMocks
    private UserAddressServiceImpl userAddressService;

    @Mock
    private UserAddressRepository userAddressRepository;

    @Mock
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .userId(1L)
                .username("tester")
                .build();
    }

    @Test
    @DisplayName("해당 userId의 모든 주소를 반환한다")
    void findAllAddresses() {
        // given
        given(userService.readUser(1L)).willReturn(mockUser);
        List<UserAddress> addresses = List.of(
                UserAddress.builder().addressId(1L).addressName("집").address("서울").detailAddress("101호").user(mockUser)
                        .build(),
                UserAddress.builder().addressId(2L).addressName("회사").address("판교").detailAddress("302호").user(mockUser)
                        .build()
        );
        given(userAddressRepository.findAllByUser(mockUser)).willReturn(addresses);

        // when
        List<UserAddressResponse> results = userAddressService.findByUserId(1L);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getAddressName()).isEqualTo("집");
        assertThat(results.get(1).getAddress()).isEqualTo("판교");
    }

    @Test
    @DisplayName("주소 ID가 존재하면 주소 정보를 반환한다")
    void findAddress_success() {
        // given
        given(userService.readUser(1L)).willReturn(mockUser);
        UserAddress address = UserAddress.builder()
                .addressId(1L).addressName("집").address("서울").detailAddress("101호").user(mockUser).build();
        given(userAddressRepository.findByUserAndAddressId(mockUser, 1L)).willReturn(Optional.of(address));

        // when
        UserAddressResponse result = userAddressService.findByUserIdAndAddressId(1L, 1L);

        // then
        assertThat(result.getAddressName()).isEqualTo("집");
    }

    @Test
    @DisplayName("주소 ID가 존재하지 않으면 예외를 던진다")
    void findAddress_fail() {
        // given
        given(userService.readUser(1L)).willReturn(mockUser);
        given(userAddressRepository.findByUserAndAddressId(mockUser, 1L)).willReturn(Optional.empty());

        // then
        assertThrows(UserAddressNotFoundException.class,
                () -> userAddressService.findByUserIdAndAddressId(1L, 1L));
    }

    @Test
    @DisplayName("새 주소를 정상 저장한다")
    void save_success() {
        // given
        UserAddressCreateRequest request = new UserAddressCreateRequest("집", "서울", "101호");
        given(userService.readUser(1L)).willReturn(mockUser);
        given(userAddressRepository.countByUser(mockUser)).willReturn(3L);
        given(userAddressRepository.existsByAddressName("집")).willReturn(false);

        // when
        userAddressService.save(1L, request);

        // then
        verify(userAddressRepository).save(any(UserAddress.class));
    }

    @Test
    @DisplayName("주소 개수가 10개 이상이면 예외 발생")
    void save_addressFull() {
        UserAddressCreateRequest request = new UserAddressCreateRequest("집", "서울", "101호");
        given(userService.readUser(1L)).willReturn(mockUser);
        given(userAddressRepository.countByUser(mockUser)).willReturn(10L);

        assertThrows(UserAddressFullException.class,
                () -> userAddressService.save(1L, request));
    }

    @Test
    @DisplayName("중복된 주소명이면 예외 발생")
    void save_duplicateName() {
        UserAddressCreateRequest request = new UserAddressCreateRequest("집", "서울", "101호");
        given(userService.readUser(1L)).willReturn(mockUser);
        given(userAddressRepository.countByUser(mockUser)).willReturn(2L);
        given(userAddressRepository.existsByAddressName("집")).willReturn(true);

        assertThrows(AlreadyExistsUserAddressException.class,
                () -> userAddressService.save(1L, request));
    }

    @Test
    @DisplayName("기존 주소 정보를 수정한다")
    void update_success() {
        // given
        UserAddress address = UserAddress.builder().addressId(1L).user(mockUser).build();
        UserAddressUpdateRequest request = new UserAddressUpdateRequest("회사", "판교", "302호");

        given(userService.readUser(1L)).willReturn(mockUser);
        given(userAddressRepository.findByUserAndAddressId(mockUser, 1L)).willReturn(Optional.of(address));

        // when
        userAddressService.update(1L, 1L, request);

        // then
        verify(userAddressRepository).save(address);
        assertThat(address.getAddressName()).isEqualTo("회사");
    }

    @Test
    @DisplayName("주소 ID가 존재하지 않으면 예외 발생")
    void update_fail_not_found() {
        UserAddressUpdateRequest request = new UserAddressUpdateRequest("회사", "판교", "302호");

        given(userService.readUser(1L)).willReturn(mockUser);
        given(userAddressRepository.findByUserAndAddressId(mockUser, 1L)).willReturn(Optional.empty());

        assertThrows(UserAddressNotFoundException.class,
                () -> userAddressService.update(1L, 1L, request));
    }

    @Test
    @DisplayName("주소 ID가 존재하면 삭제한다")
    void delete_success() {
        // given
        UserAddress address = UserAddress.builder().addressId(1L).user(mockUser).build();
        given(userService.readUser(1L)).willReturn(mockUser);
        given(userAddressRepository.findByUserAndAddressId(mockUser, 1L)).willReturn(Optional.of(address));

        // when
        userAddressService.deleteByUserIdAndAddressId(1L, 1L);

        // then
        verify(userAddressRepository).delete(address);
    }

    @Test
    @DisplayName("주소 ID가 없으면 예외 발생")
    void delete_not_found() {
        given(userService.readUser(1L)).willReturn(mockUser);
        given(userAddressRepository.findByUserAndAddressId(mockUser, 1L)).willReturn(Optional.empty());

        assertThrows(UserAddressNotFoundException.class,
                () -> userAddressService.deleteByUserIdAndAddressId(1L, 1L));
    }

}
