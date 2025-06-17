package shop.wannab.userservice.address.service.Impl;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.wannab.userservice.address.domain.dto.UserAddressCreateRequest;
import shop.wannab.userservice.address.domain.dto.UserAddressResponse;
import shop.wannab.userservice.address.domain.dto.UserAddressUpdateRequest;
import shop.wannab.userservice.address.domain.entity.UserAddress;
import shop.wannab.userservice.address.exception.UserAddressNotFoundException;
import shop.wannab.userservice.address.repository.UserAddressRepository;
import shop.wannab.userservice.address.service.UserAddressService;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.service.UserService;

@RequiredArgsConstructor
@Service
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserService userService;

    @Override
    @Transactional
    public List<UserAddressResponse> findByUserId(Long userId) {
        List<UserAddress> userAddresses = userAddressRepository.findAll();
        return userAddresses.stream()
                .map(userAddress -> UserAddressResponse.builder()
                        .addressId(userAddress.getAddressId())
                        .addressName(userAddress.getAddressName())
                        .address(userAddress.getAddress())
                        .detailAddress(userAddress.getDetailAddress())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserAddressResponse findByUserIdAndAddressId(Long userId, Long addressId) {
        User user = userService.readUser(userId);
        UserAddress entity = userAddressRepository.findByUserAndAddressId(user, addressId)
                .orElseThrow(UserAddressNotFoundException::new);

        return UserAddressResponse.builder()
                .addressId(entity.getAddressId())
                .addressName(entity.getAddressName())
                .address(entity.getAddress())
                .detailAddress(entity.getDetailAddress())
                .build();
    }

    @Override
    @Transactional
    public UserAddress save(Long userId, UserAddressCreateRequest request) {
        UserAddress entity = UserAddress.builder()
                .addressName(request.getAddressName())
                .address(request.getAddress())
                .detailAddress(request.getDetailAddress())
                .user(userService.readUser(userId))
                .build();

        return userAddressRepository.save(entity);
    }

    @Override
    @Transactional
    public UserAddress update(Long userId, Long addressId, UserAddressUpdateRequest request) {
        User user = userService.readUser(userId);
        UserAddress entity = userAddressRepository.findByUserAndAddressId(user, addressId)
                .orElseThrow(UserAddressNotFoundException::new);

        entity.setAddressName(request.getAddressName());
        entity.setAddress(request.getAddress());
        entity.setDetailAddress(request.getDetailAddress());

        return userAddressRepository.save(entity);
    }

    @Override
    @Transactional
    public void deleteByUserIdAndAddressId(Long userId, Long addressId) {
        User user = userService.readUser(userId);
        UserAddress entity = userAddressRepository.findByUserAndAddressId(user, addressId)
                .orElseThrow(UserAddressNotFoundException::new);

        userAddressRepository.delete(entity);
    }
}
