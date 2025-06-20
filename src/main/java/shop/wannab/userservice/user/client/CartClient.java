package shop.wannab.userservice.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "cart-service", url = "http://localhost:8080")
public interface CartClient {
    @PostMapping("/api/cart")
    void createCart();
}
