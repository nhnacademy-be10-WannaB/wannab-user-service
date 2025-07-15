package shop.wannab.userservice.global.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private static final String EXCHANGE_NAME = "wannab.user.exchange";
    private static final String QUEUE_NAME = "wannab.welcome.coupon.queue";
    private static final String ROUTING_KEY = "user.signup.event";

    public static final String ORDER_QUEUE = "wannab.order.created.user.queue";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue welcomeQueue(){
        return new Queue(QUEUE_NAME);
    }

    @Bean
    public Queue orderUserQueue() {
        return new Queue(ORDER_QUEUE);
    }

}
