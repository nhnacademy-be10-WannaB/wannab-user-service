package shop.wannab.userservice.user.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.userservice.user.domain.dto.request.UserUpdateRequest;
import shop.wannab.userservice.user.domain.dto.response.UserPageResponse;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.service.UserService;
import shop.wannab.userservice.utils.Util;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;


    @GetMapping
    public ResponseEntity<UserPageResponse> readUser(@RequestHeader(Util.HEADER_ID_NAME) Long userId) {
        log.info("Controller: readUser");
        UserPageResponse response = userService.readUserPageResponse(userId);

        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<UserPageResponse> updateUser(@RequestHeader(Util.HEADER_ID_NAME) Long userId,
                                                       @RequestBody @Valid UserUpdateRequest userupdateDTO) {
        log.info("Controller: updateUser");
        User user = userService.updateUser(userId, userupdateDTO);
        UserPageResponse response = UserPageResponse.builder()
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birth(user.getBirth())
                .nickname(user.getNickname())
                .password(user.getPassword())
                .points(user.getPoints())
                .grade(user.getUserGrade().getGradeName())
                .build();
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@RequestHeader(Util.HEADER_ID_NAME) Long userId) {
        log.info("Controller: deleteUser");
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/logout")
    public ResponseEntity<User> logout(@RequestHeader(Util.HEADER_ID_NAME) Long userId) {
        log.info("Controller: logout");
        userService.logout(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/birthdays")
    public List<Long> birthUserList(@RequestParam int month) {
        log.info("Controller: birthUserList");
        return userService.birthUserList(month);
    }
}
