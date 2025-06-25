package shop.wannab.userservice.address.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.userservice.address.domain.dto.UserAddressCreateRequest;
import shop.wannab.userservice.address.domain.dto.UserAddressResponse;
import shop.wannab.userservice.address.domain.dto.UserAddressUpdateRequest;
import shop.wannab.userservice.address.domain.entity.UserAddress;
import shop.wannab.userservice.address.service.UserAddressService;
import shop.wannab.userservice.user.service.UserService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users/addresses")
public class UserAddressController {

    private final UserAddressService userAddressService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserAddressResponse>> getAllAddresses(@RequestHeader("X-USER-ID") Long userId) {
        List<UserAddressResponse> addresses = userAddressService.findByUserId(userId);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{address-id}")
    public ResponseEntity<UserAddressResponse> getAddress(@RequestHeader("X-USER-ID") Long userId,
                                                          @PathVariable(name = "address-id") Long addressId) {
        UserAddressResponse address = userAddressService.findByUserIdAndAddressId(userId, addressId);
        return ResponseEntity.ok(address);
    }

    @PostMapping
    public ResponseEntity<UserAddress> createAddress(@RequestHeader("X-USER-ID") Long userId,
                                                     @RequestBody @Valid UserAddressCreateRequest userAddressCreateRequest) {
        UserAddress saved = userAddressService.save(userId, userAddressCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{address-id}")
    public ResponseEntity<UserAddress> updateAddress(@RequestHeader("X-USER-ID") Long userId,
                                                     @PathVariable(name = "address-id") Long addressId,
                                                     @RequestBody @Valid UserAddressUpdateRequest userAddressUpdateRequest) {
        UserAddress userAddress = userAddressService.update(userId, addressId, userAddressUpdateRequest);
        return ResponseEntity.ok(userAddress);
    }

    @DeleteMapping("/{address-id}")
    public ResponseEntity<Void> deleteAddress(@RequestHeader("X-USER-ID") Long userId,
                                              @PathVariable(name = "address-id") Long addressId) {
        userAddressService.deleteByUserIdAndAddressId(userId, addressId);
        return ResponseEntity.noContent().build();
    }
}

