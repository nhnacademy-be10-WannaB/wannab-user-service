package shop.wannab.userservice.address.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.userservice.address.domain.dto.UserAddressCreateRequest;
import shop.wannab.userservice.address.domain.dto.UserAddressResponse;
import shop.wannab.userservice.address.domain.dto.UserAddressUpdateRequest;
import shop.wannab.userservice.address.domain.entity.UserAddress;
import shop.wannab.userservice.address.exception.UserAddressFullException;
import shop.wannab.userservice.address.exception.UserAddressNotFoundException;
import shop.wannab.userservice.address.repository.UserAddressRepository;
import shop.wannab.userservice.address.service.UserAddressService;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.service.UserService;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public List<UserAddressResponse> findByUserId(Long userId) {
        log.info("Service: findByUserId");
        List<UserAddress> userAddresses = userAddressRepository.findAllByUser(userService.readUser(userId));
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
    @Transactional(readOnly = true)
    public UserAddressResponse findByUserIdAndAddressId(Long userId, Long addressId) {
        log.info("Service: findByUserIdAndAddressId");
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
    public void save(Long userId, UserAddressCreateRequest request) {
        log.info("Service: save");
        User user = userService.readUser(userId);
        long addressCount = userAddressRepository.countByUser(user);
        if (addressCount >= 10) {
            throw new UserAddressFullException("주소는 최대 10개까지 등록할 수 있습니다.");
        }
        UserAddress entity = UserAddress.builder()
                .addressName(request.getAddressName())
                .address(request.getAddress())
                .detailAddress(request.getDetailAddress())
                .user(userService.readUser(userId))
                .build();
        userAddressRepository.save(entity);

    }

    @Override
    @Transactional
    public void update(Long userId, Long addressId, UserAddressUpdateRequest request) {
        log.info("Service: update");
        User user = userService.readUser(userId);
        UserAddress entity = userAddressRepository.findByUserAndAddressId(user, addressId)
                .orElseThrow(UserAddressNotFoundException::new);

        entity.setAddressName(request.getAddressName());
        entity.setAddress(request.getAddress());
        entity.setDetailAddress(request.getDetailAddress());

        userAddressRepository.save(entity);
    }

    @Override
    @Transactional
    public void deleteByUserIdAndAddressId(Long userId, Long addressId) {
        log.info("Service: deleteByUserIdAndAddressId");
        User user = userService.readUser(userId);
        UserAddress entity = userAddressRepository.findByUserAndAddressId(user, addressId)
                .orElseThrow(UserAddressNotFoundException::new);

        userAddressRepository.delete(entity);
    }
}
