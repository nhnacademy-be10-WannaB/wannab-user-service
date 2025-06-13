package shop.wannab.userservice.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.userservice.user.domain.dto.CommonResponse;
import shop.wannab.userservice.user.domain.dto.Status;
import shop.wannab.userservice.user.domain.dto.UserCreateDTO;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.exception.UserIdMismatchException;
import shop.wannab.userservice.user.service.UserService;
import shop.wannab.userservice.util.Util;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @PostMapping()
    public ResponseEntity<CommonResponse<?>> createUser(@RequestBody @Valid UserCreateDTO userCreateDTO) {
        CommonResponse<?> response = new CommonResponse<>();
        userService.createUser(userCreateDTO);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{user-id}")
    public ResponseEntity<CommonResponse<User>> readUser(@PathVariable(name = "user-id") String userId,
                                                         @RequestHeader(Util.HEADER_ID_NAME) String headerUserId) {
        if (!userId.equals(headerUserId)) {
            throw new UserIdMismatchException();
        }
        CommonResponse<User> response = new CommonResponse<>(
                Status.SUCCESS,
                userService.readUser(Long.parseLong(userId)),
                null);
        return ResponseEntity.ok().body(response);
    }

//    @PatchMapping("/{user-id}")
//    public ResponseEntity<CommonResponse<?>> updateUser(@PathVariable(name = "user-id") String userId,
//                                                        @RequestHeader(Util.HEADER_ID_NAME) String headerUserId,
//                                                        @RequestBody @Valid UserUpdateDTO userupdateDTO) {
//        if (!userId.equals(headerUserId)) {
//            throw new UserIdMismatchException();
//        }
//        userService.updateUser(userupdateDTO);
//    }


}
