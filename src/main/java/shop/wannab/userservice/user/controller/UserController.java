package shop.wannab.userservice.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.userservice.user.domain.dto.request.UserUpdateRequest;
import shop.wannab.userservice.user.domain.dto.response.UserPageResponse;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.service.UserService;
import shop.wannab.userservice.utils.Util;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;


    @GetMapping
    public ResponseEntity<UserPageResponse> readUser(@RequestHeader(Util.HEADER_ID_NAME) Long userId) {
        User user = userService.readUser(userId);
        UserPageResponse response = UserPageResponse.builder()
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birth(user.getBirth())
                .nickname(user.getNickname())
                .password(user.getPassword())
                .points(user.getPoints())
                .build();
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping
    public ResponseEntity<User> updateUser(@RequestHeader(Util.HEADER_ID_NAME) Long userId,
                                           @RequestBody @Valid UserUpdateRequest userupdateDTO) {
        User user = userService.updateUser(userId, userupdateDTO);
        return ResponseEntity.ok().body(user);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@RequestHeader(Util.HEADER_ID_NAME) Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/logout")
    public ResponseEntity<User> logout(@RequestHeader(Util.HEADER_ID_NAME) Long userId) {
        userService.logout(userId);
        return ResponseEntity.noContent().build();
    }
}
