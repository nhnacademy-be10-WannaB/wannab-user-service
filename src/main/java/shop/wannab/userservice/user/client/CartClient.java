package shop.wannab.userservice.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shop.wannab.userservice.user.domain.dto.CartCreateRequest;

@FeignClient(name = "order-payment-service", contextId = "cartClient")
public interface CartClient {

    @PostMapping("/api/cart")
    void createCart(@RequestBody CartCreateRequest cartCreateRequest);
}
