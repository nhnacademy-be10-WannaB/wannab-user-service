package shop.wannab.userservice.global.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    private static final String EXCHANGE_NAME = "wannab.user.exchange";
    private static final String ROUTING_KEY = "user.signup.event";

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new SimpleMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue welcomeQueue(){
        return new Queue("${welcome.coupon.queue}");
    }

    @Bean
    public Binding welcomeQueueBinding(DirectExchange exchange, Queue welcomeQueue){
        return BindingBuilder.bind(welcomeQueue).to(exchange).with(ROUTING_KEY);
    }

}
