package shop.wannab.userservice.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import shop.wannab.userservice.user.client.CartClient;
import shop.wannab.userservice.user.domain.dto.request.CartCreateRequest;
import shop.wannab.userservice.user.domain.entity.User;

@Service
@RequiredArgsConstructor
public class PostSignupServiceImpl implements PostSignupService {
    private final CartClient cartClient;
    private final RabbitTemplate rabbitTemplate;

    @Async
    @Override
    public void handlePostSignup(User user){
        rabbitTemplate.convertAndSend("wannab.user.exchange", "user.signup.event", String.valueOf(user.getUserId()));
        cartClient.createCart(new CartCreateRequest(user.getUserId()));
    }
}
