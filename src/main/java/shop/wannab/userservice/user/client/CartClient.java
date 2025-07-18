package shop.wannab.userservice.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shop.wannab.userservice.user.domain.dto.request.CartCreateRequest;

@FeignClient(name = "order-payment-service", contextId = "cartClient", url = "${order.api.url}")
public interface CartClient {

    @PostMapping("/api/cart")
    void createCart(@RequestBody CartCreateRequest cartCreateRequest);
}
