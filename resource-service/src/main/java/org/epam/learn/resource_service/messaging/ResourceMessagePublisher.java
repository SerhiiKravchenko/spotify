package org.epam.learn.resource_service.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ResourceMessagePublisher {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;

    public ResourceMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishResourceUploaded(Long resourceId) {
        rabbitTemplate.convertAndSend(exchange, routingKey, new ResourceUploadedMessage(resourceId));
    }

    public record ResourceUploadedMessage(Long resourceId) {}
}
