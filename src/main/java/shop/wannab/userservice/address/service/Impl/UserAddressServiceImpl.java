package shop.wannab.userservice.address.service.Impl;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.userservice.address.domain.dto.request.UserAddressCreateRequest;
import shop.wannab.userservice.address.domain.dto.request.UserAddressUpdateRequest;
import shop.wannab.userservice.address.domain.dto.response.UserAddressResponse;
import shop.wannab.userservice.address.domain.entity.UserAddress;
import shop.wannab.userservice.address.exception.AlreadyExistsUserAddressException;
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
        log.info("action=findByUserId, userId={}, message=\"유저아이디로 주소 리스트 찾기 시작\"", userId);
        List<UserAddress> userAddresses = userAddressRepository.findAllByUser(userService.readUser(userId));
        log.info("action=findByUserId, userId={}, message=\"유저아이디로 주소 리스트 찾기 완료\"", userId);
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
        log.info("action=findByUserIdAndAddressId, userId={}, addressId={}, message=\"유저아이디로 주소 찾기 시작\"", userId,
                addressId);
        User user = userService.readUser(userId);

        UserAddress entity = userAddressRepository.findByUserAndAddressId(user, addressId)
                .orElseThrow(UserAddressNotFoundException::new);
        log.info("action=findByUserIdAndAddressId, userId={}, addressId={}, message=\"유저아이디로 주소 찾기 완료\"", userId,
                addressId);

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
        log.info("action=save, userId={}, message=\"주소 생성 시작\"", userId);
        User user = userService.readUser(userId);
        long addressCount = userAddressRepository.countByUser(user);
        if (addressCount >= 10) {
            throw new UserAddressFullException("주소는 최대 10개까지 등록할 수 있습니다.");
        } else if (userAddressRepository.existsByAddressName(request.getAddressName())) {
            throw new AlreadyExistsUserAddressException("이미 존재하는 주소입니다.");
        }
        UserAddress entity = UserAddress.builder()
                .addressName(request.getAddressName())
                .address(request.getAddress())
                .detailAddress(request.getDetailAddress())
                .user(userService.readUser(userId))
                .build();
        userAddressRepository.save(entity);
        log.info("action=save, userId={}, message=\"주소 생성 완료\"", userId);

    }

    @Override
    @Transactional
    public void update(Long userId, Long addressId, UserAddressUpdateRequest request) {
        log.info("action=update, userId={}, addressId={}, message=\"주소 수정 시작\"", userId, addressId);
        User user = userService.readUser(userId);
        UserAddress entity = userAddressRepository.findByUserAndAddressId(user, addressId)
                .orElseThrow(UserAddressNotFoundException::new);

        entity.setAddressName(request.getAddressName());
        entity.setAddress(request.getAddress());
        entity.setDetailAddress(request.getDetailAddress());

        userAddressRepository.save(entity);
        log.info("action=update, userId={}, addressId={}, message=\"주소 수정 완료\"", userId, addressId);
    }

    @Override
    @Transactional
    public void deleteByUserIdAndAddressId(Long userId, Long addressId) {
        log.info("action=deleteByUserIdAndAddressId, userId={}, addressId={}, message=\"주소 삭제 시작\"", userId, addressId);
        User user = userService.readUser(userId);
        UserAddress entity = userAddressRepository.findByUserAndAddressId(user, addressId)
                .orElseThrow(UserAddressNotFoundException::new);

        userAddressRepository.delete(entity);
        log.info("action=deleteByUserIdAndAddressId, userId={}, addressId={}, message=\"주소 삭제 완료\"", userId, addressId);
    }
}
