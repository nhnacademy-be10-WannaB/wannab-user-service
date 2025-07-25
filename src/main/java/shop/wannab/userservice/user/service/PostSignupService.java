package shop.wannab.userservice.user.service;

import org.springframework.scheduling.annotation.Async;
import shop.wannab.userservice.user.domain.entity.User;

public interface PostSignupService {
    @Async
    void handlePostSignup(User user);
}
