package shop.wannab.userservice.auth;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shop.wannab.userservice.auth.dto.request.SendMessageRequest;

@FeignClient(
        name = "doorayMessageClient",
        url = "https://nhnacademy.dooray.com"
)
public interface DoorayMessageClient {

    @PostMapping("/services/3204376758577275363/4094430404194879315/qtRDdsQ5TWuqQrXgavQE5g")
    ResponseEntity<Void> sendUnlockCode(@RequestBody SendMessageRequest request);

}