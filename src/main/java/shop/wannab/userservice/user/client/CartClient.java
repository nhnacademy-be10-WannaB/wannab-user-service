package shop.wannab.userservice.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "order-payment-service", contextId = "cartClient")
public interface CartClient {

    @PostMapping("/api/cart")
    void createCart();
}
