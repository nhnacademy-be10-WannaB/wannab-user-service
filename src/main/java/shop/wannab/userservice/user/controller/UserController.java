package shop.wannab.userservice.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.userservice.user.domain.dto.CommonResponse;
import shop.wannab.userservice.user.domain.dto.UserCreateDTO;
import shop.wannab.userservice.user.service.UserService;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/api/users")
    public ResponseEntity<CommonResponse<?>> createUser(@RequestBody UserCreateDTO userCreateDTO) {
        CommonResponse<?> response = new CommonResponse<>();
        userService.createUser(userCreateDTO);
        return ResponseEntity.ok().body(response);
    }

//    @GetMapping("/api/users/{user-id}")
//    public ResponseEntity<CommonResponse<User>> readUser(@PathVariable String userId, @RequestHeader(Util.HEADER_ID_NAME) String headerUserId) {
//        if (!userId.equals(headerUserId)) {
//            throw
//        }
//        userService.findById(userId);
//        return ResponseEntity.ok().body();
//    }
//    @PostMapping("/api/user/login")
//    public ResponseEntity<> login(){
//
//    }
}
