package shop.wannab.userservice.address.service;

import java.util.List;
import shop.wannab.userservice.address.domain.dto.UserAddressCreateRequest;
import shop.wannab.userservice.address.domain.dto.UserAddressResponse;
import shop.wannab.userservice.address.domain.dto.UserAddressUpdateRequest;
import shop.wannab.userservice.address.domain.entity.UserAddress;

public interface UserAddressService {
    // TODO: dto로 변경
    List<UserAddressResponse> findByUserId(Long userId);

    UserAddressResponse findByUserIdAndAddressId(Long userId, Long addressId);

    UserAddress save(Long userId, UserAddressCreateRequest request);

    UserAddress update(Long userId, Long addressId, UserAddressUpdateRequest request);

    void deleteByUserIdAndAddressId(Long userId, Long addressId);
}
