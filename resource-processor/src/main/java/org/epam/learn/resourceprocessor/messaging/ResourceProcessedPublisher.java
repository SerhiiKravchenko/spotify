package org.epam.learn.resourceprocessor.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Component;

@Component
public class ResourceProcessedPublisher {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.processed-routing-key}")
    private String processedRoutingKey;

    private final RabbitTemplate rabbitTemplate;
    private final RetryTemplate retryTemplate;

    public ResourceProcessedPublisher(RabbitTemplate rabbitTemplate, RetryTemplate retryTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.retryTemplate = retryTemplate;
    }

    public void publishResourceProcessed(Long resourceId) {
        retryTemplate.invoke(() ->
                rabbitTemplate.convertAndSend(exchange, processedRoutingKey, new ResourceProcessedMessage(resourceId)));
    }

    public record ResourceProcessedMessage(Long resourceId) {
    }
}
