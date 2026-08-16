package org.epam.learn.resourceprocessor.configuration;

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

    @Value("${rabbitmq.processed-queue}")
    private String processedQueue;

    @Value("${rabbitmq.processed-routing-key}")
    private String processedRoutingKey;

    @Bean
    public DirectExchange resourceExchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    public Queue resourceQueue() {
        return new Queue(queue, true);
    }

    @Bean
    public Queue resourceProcessedQueue() {
        return new Queue(processedQueue, true);
    }

    @Bean
    public Binding resourceProcessedBinding(Queue resourceProcessedQueue, DirectExchange resourceExchange) {
        return BindingBuilder.bind(resourceProcessedQueue).to(resourceExchange).with(processedRoutingKey);
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
