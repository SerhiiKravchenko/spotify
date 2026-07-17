package org.epam.learn.resource_service.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queue}")
    private String queue;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    @Bean
    public DirectExchange resourceExchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    public Queue resourceQueue() {
        return new Queue(queue, true);
    }

    @Bean
    public Binding resourceBinding(Queue resourceQueue, DirectExchange resourceExchange) {
        return BindingBuilder.bind(resourceQueue).to(resourceExchange).with(routingKey);
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
