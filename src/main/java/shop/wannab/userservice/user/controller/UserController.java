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
import shop.wannab.userservice.utils.HeaderUtil;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;


    @GetMapping
    public ResponseEntity<UserPageResponse> readMyPageUser(@RequestHeader(HeaderUtil.HEADER_ID_NAME) Long userId) {
        UserPageResponse response = userService.readUserPageResponse(userId);
        log.info("action=readMyPageUser, userId={}, message=\"마이페이지 조회 완료 \"", userId);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<UserPageResponse> updateUser(@RequestHeader(HeaderUtil.HEADER_ID_NAME) Long userId,
                                                       @RequestBody @Valid UserUpdateRequest userUpdateDTO) {
        UserPageResponse response = userService.updateUser(userId, userUpdateDTO);
        log.info("action=updateUser, userId={}, message=\"사용자 정보 업데이트 완료\"", userId);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@RequestHeader(HeaderUtil.HEADER_ID_NAME) Long userId) {
        userService.deleteUser(userId);
        log.info("action=deleteUser, userId={}, message=\"사용자 계정 삭제 완료\"", userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/logout")
    public ResponseEntity<User> logout(@RequestHeader(HeaderUtil.HEADER_ID_NAME) Long userId) {
        userService.logout(userId);
        log.info("action=logout, userId={}, message=\"사용자 로그아웃 완료\"", userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/birthdays")
    public List<Long> birthUserList(@RequestParam int month) {
        List<Long> result = userService.birthUserList(month);
        log.info("action=birthUserList, month={}, userCount={}, message=\"생일자 사용자 목록 조회 완료\"", month, result.size());
        return result;
    }


}
