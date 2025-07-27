package shop.wannab.userservice.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.userservice.point.domain.dto.response.PageResponse;
import shop.wannab.userservice.user.domain.dto.request.AdminUserUpdateRequest;
import shop.wannab.userservice.user.domain.dto.response.AdminPageUserResponse;
import shop.wannab.userservice.user.service.UserService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<PageResponse<AdminPageUserResponse>> userList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AdminPageUserResponse> adminPageUserResponses = userService.readUserList(page, size);
        log.info("action=userList, page={}, size={}, totalElements={}, message=\"관리자용 사용자 목록 조회 완료\"", page, size, adminPageUserResponses.getTotalElements());
        return ResponseEntity.ok(PageResponse.from(adminPageUserResponses));
    }

    @GetMapping("/{login-id}")
    public ResponseEntity<AdminPageUserResponse> readAdminPageUser(@PathVariable("login-id") String loginId) {
        AdminPageUserResponse adminPageUserResponse = userService.readAdminPageUser(loginId);
        log.info("action=readAdminPageUser, loginId=\"{}\", message=\"관리자용 특정 사용자 정보 조회 완료\"", loginId);
        return ResponseEntity.ok(adminPageUserResponse);
    }

    @PutMapping("/{login-id}")
    public ResponseEntity<Void> updateAdminPageUser(@PathVariable("login-id") String loginId,
                                                    @RequestBody AdminUserUpdateRequest adminUserUpdateRequest) {
        userService.updateAdminUser(loginId, adminUserUpdateRequest);
        log.info("action=updateAdminPageUser, loginId=\"{}\", message=\"관리자용 사용자 정보 업데이트 완료\"", loginId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{login-id}")
    public ResponseEntity<Void> deleteAdminPageUser(@PathVariable("login-id") String loginId) {
        userService.deleteAdminUser(loginId);
        log.info("action=deleteAdminPageUser, loginId=\"{}\", message=\"관리자용 사용자 계정 삭제 완료\"", loginId);
        return ResponseEntity.ok().build();
    }
}
