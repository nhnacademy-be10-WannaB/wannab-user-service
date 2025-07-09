package shop.wannab.userservice.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "coupon-service",contextId = "couponClient")
public interface CouponClient {
    @PostMapping("/api/coupons/issue/welcome")
    void issueWelcomeCoupon(@RequestParam Long userId);
}
