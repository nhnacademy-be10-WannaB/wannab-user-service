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
import shop.wannab.userservice.address.domain.dto.request.UserAddressUpdateRequest;
import shop.wannab.userservice.address.domain.dto.response.UserAddressResponse;
import shop.wannab.userservice.address.exception.AlreadyExistsUserAddressException;
import shop.wannab.userservice.address.exception.UserAddressFullException;
import shop.wannab.userservice.address.exception.UserAddressNotFoundException;
import shop.wannab.userservice.address.service.UserAddressService;
import shop.wannab.userservice.global.Response;
import shop.wannab.userservice.utils.ErrorCode;
import shop.wannab.userservice.utils.ResponseCode;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users/addresses")
public class UserAddressController {

    private final UserAddressService userAddressService;

    @GetMapping
    public ResponseEntity<List<UserAddressResponse>> getAllAddresses(@RequestHeader("X-USER-ID") Long userId) {
        List<UserAddressResponse> addresses = userAddressService.findByUserId(userId);
        log.info("action=getAllAddress, userId={}, message=\"사용자 아이디로 주소 리스트 조회 완료\"", userId);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{address-id}")
    public ResponseEntity<UserAddressResponse> getAddress(@RequestHeader("X-USER-ID") Long userId,
                                                          @PathVariable(name = "address-id") Long addressId) {
        UserAddressResponse address = userAddressService.findByUserIdAndAddressId(userId, addressId);
        log.info("action=getAddress, userId={}, addressId={} message=\"사용자 아이디로 주소 조회 완료\"", userId, addressId);
        return ResponseEntity.ok(address);
    }

    @PostMapping
    public Response<Void> createAddress(@RequestHeader("X-USER-ID") Long userId,
                                        @RequestBody @Valid UserAddressCreateRequest userAddressCreateRequest) {
        userAddressService.save(userId, userAddressCreateRequest);
        log.info("action=createAddress, userId={}, message=\"주소 생성 완료\"", userId);
        return new Response<>(null, ResponseCode.SUCCESS, null);
    }

    @PutMapping("/{address-id}")
    public Response<Void> updateAddress(@RequestHeader("X-USER-ID") Long userId,
                                        @PathVariable(name = "address-id") Long addressId,
                                        @RequestBody @Valid UserAddressUpdateRequest userAddressUpdateRequest) {
        userAddressService.update(userId, addressId, userAddressUpdateRequest);
        log.info("action=updateAddress, userId={}, addressId={} message=\"주소 수정 완료\"", userId, addressId);
        return new Response<>(null, ResponseCode.SUCCESS, null);
    }

    @DeleteMapping("/{address-id}")
    public Response<Void> deleteAddress(@RequestHeader("X-USER-ID") Long userId,
                                        @PathVariable(name = "address-id") Long addressId) {
        userAddressService.deleteByUserIdAndAddressId(userId, addressId);
        log.info("action=deleteAddress, userId={}, addressId={} message=\"주소 삭제 완료\"", userId, addressId);
        return new Response<>(null, ResponseCode.SUCCESS, null);
    }


    @ExceptionHandler({UserAddressFullException.class})
    public Response<Void> handleUserAddressFullException(Exception e) {
        log.warn("statusCode: {}, errorCode : {}, message : {}", 400, ErrorCode.ADDRESS_IS_FULL, "주소가 최대치(10개)입니다.");
        return new Response<>(null, ResponseCode.ADDRESS_IS_FULL, e.getMessage());
    }

    @ExceptionHandler({AlreadyExistsUserAddressException.class})
    public Response<String> handleAlreadyExistsUserAddressException(RuntimeException e) {
        log.warn("statusCode: {}, errorCode : {}, message : {}", 409, ErrorCode.ADDRESS_ALREADY_EXISTS,
                "이미 존재하는 주소입니다.");
        return new Response<>(null, ResponseCode.ADDRESS_ALREADY_EXISTS, e.getMessage());
    }

    @ExceptionHandler({UserAddressNotFoundException.class})
    public Response<String> handleUserAddressNotFoundException(Exception e) {
        log.warn("statusCode: {}, errorCode : {}, message : {}", 404, ErrorCode.ADDRESS_NOT_FOUND, "해당 주소가 존재하지 않습니다.");
        return new Response<>(null, ResponseCode.ADDRESS_NOT_FOUND, e.getMessage());
    }
}

