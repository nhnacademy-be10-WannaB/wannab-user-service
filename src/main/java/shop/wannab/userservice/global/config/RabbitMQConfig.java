package shop.wannab.userservice.global.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private static final String EXCHANGE_NAME = "wannab.user.exchange";
    private static final String QUEUE_NAME = "wannab.welcome.coupon.queue";
    private static final String ROUTING_KEY = "user.signup.event";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue welcomeQueue() {
        return new Queue(QUEUE_NAME);
    }

}
