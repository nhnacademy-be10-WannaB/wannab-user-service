package shop.wannab.userservice.user.controller;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.userservice.user.domain.dto.UserCreateDTO;
import shop.wannab.userservice.user.domain.dto.UserUpdateDTO;
import shop.wannab.userservice.user.domain.entity.User;
import shop.wannab.userservice.user.exception.UserIdMismatchException;
import shop.wannab.userservice.user.service.UserService;
import shop.wannab.userservice.utils.Util;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;


    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody @Valid UserCreateDTO userCreateDTO) {
        User user = userService.createUser(userCreateDTO);
        URI uri = URI.create("/api/users/" + user.getUserId());
        return ResponseEntity.created(uri).body(user);
    }

    @GetMapping("/{user-id}")
    public ResponseEntity<User> readUser(@PathVariable(name = "user-id") Long userId,
                                         @RequestHeader(Util.HEADER_ID_NAME) Long headerUserId) {
        if (!userId.equals(headerUserId)) {
            throw new UserIdMismatchException("요청자 id와 대상 id가 일치하지 않습니다");
        }
        User user = userService.readUser(userId);
        return ResponseEntity.ok().body(user);
    }

    @PatchMapping("/{user-id}")
    public ResponseEntity<User> updateUser(@PathVariable(name = "user-id") Long userId,
                                           @RequestHeader(Util.HEADER_ID_NAME) Long headerUserId,
                                           @RequestBody @Valid UserUpdateDTO userupdateDTO) {
        if (!userId.equals(headerUserId)) {
            throw new UserIdMismatchException("요청자 id와 대상 id가 일치하지 않습니다");
        }
        User user = userService.updateUser(userId, userupdateDTO);
        return ResponseEntity.ok().body(user);
    }

    @DeleteMapping("/{user-id}")
    public ResponseEntity<Void> deleteUser(@PathVariable(name = "user-id") Long userId,
                                           @RequestHeader(Util.HEADER_ID_NAME) Long headerUserId) {
        if (!userId.equals(headerUserId)) {
            throw new UserIdMismatchException("요청자 id와 대상 id가 일치하지 않습니다");
        }
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
