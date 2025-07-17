package shop.wannab.userservice.address.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.userservice.address.domain.dto.request.UserAddressCreateRequest;
import shop.wannab.userservice.address.domain.dto.response.UserAddressResponse;
import shop.wannab.userservice.address.domain.dto.request.UserAddressUpdateRequest;
import shop.wannab.userservice.address.exception.AlreadyExistsUserAddressException;
import shop.wannab.userservice.address.exception.UserAddressFullException;
import shop.wannab.userservice.address.exception.UserAddressNotFoundException;
import shop.wannab.userservice.address.service.UserAddressService;
import shop.wannab.userservice.global.Response;
import shop.wannab.userservice.utils.ResponseCode;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users/addresses")
public class UserAddressController {

    private final UserAddressService userAddressService;

    @GetMapping
    public ResponseEntity<List<UserAddressResponse>> getAllAddresses(@RequestHeader("X-USER-ID") Long userId) {
        log.info("Controller: getAllAddresses");
        List<UserAddressResponse> addresses = userAddressService.findByUserId(userId);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{address-id}")
    public ResponseEntity<UserAddressResponse> getAddress(@RequestHeader("X-USER-ID") Long userId,
                                                          @PathVariable(name = "address-id") Long addressId) {
        log.info("Controller: getAddress");
        UserAddressResponse address = userAddressService.findByUserIdAndAddressId(userId, addressId);
        return ResponseEntity.ok(address);
    }

    @PostMapping
    public Response<Void> createAddress(@RequestHeader("X-USER-ID") Long userId,
                                        @RequestBody @Valid UserAddressCreateRequest userAddressCreateRequest) {
        log.info("Controller: createAddress");
        userAddressService.save(userId, userAddressCreateRequest);
        return new Response<>(null, ResponseCode.SUCCESS, null);
    }

    @PutMapping("/{address-id}")
    public Response<Void> updateAddress(@RequestHeader("X-USER-ID") Long userId,
                                        @PathVariable(name = "address-id") Long addressId,
                                        @RequestBody @Valid UserAddressUpdateRequest userAddressUpdateRequest) {
        log.info("Controller: updateAddress");
        userAddressService.update(userId, addressId, userAddressUpdateRequest);
        return new Response<>(null, ResponseCode.SUCCESS, null);
    }

    @DeleteMapping("/{address-id}")
    public Response<Void> deleteAddress(@RequestHeader("X-USER-ID") Long userId,
                                        @PathVariable(name = "address-id") Long addressId) {
        log.info("Controller: deleteAddress");
        userAddressService.deleteByUserIdAndAddressId(userId, addressId);
        return new Response<>(null, ResponseCode.SUCCESS, null);
    }


    @ExceptionHandler({UserAddressFullException.class})
    public Response<Void> handleUserAddressFullException(Exception e) {
        log.info("Exception: handleUserAddressFullException");
        return new Response<>(null, ResponseCode.ADDRESS_IS_FULL, e.getMessage());
    }

    @ExceptionHandler({AlreadyExistsUserAddressException.class})
    public Response<String> handleAlreadyExistsUserAddressException(RuntimeException e) {
        log.info("Exception: handleAlreadyExistsUserAddressException");
        return new Response<>(null, ResponseCode.ADDRESS_ALREADY_EXISTS, e.getMessage());
    }

    @ExceptionHandler({UserAddressNotFoundException.class})
    public Response<String> handleUserAddressNotFoundException(Exception e) {
        log.info("Exception: handleUserAddressNotFoundException");
        return new Response<>(null, ResponseCode.ADDRESS_NOT_FOUND, e.getMessage());
    }
}

