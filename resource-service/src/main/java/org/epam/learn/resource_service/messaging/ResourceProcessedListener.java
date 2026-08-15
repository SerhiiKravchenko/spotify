package org.epam.learn.resource_service.messaging;

import org.epam.learn.resource_service.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ResourceProcessedListener {

    private static final Logger log = LoggerFactory.getLogger(ResourceProcessedListener.class);

    private final ResourceService resourceService;

    public ResourceProcessedListener(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @RabbitListener(queues = "${rabbitmq.processed-queue}")
    public void handleResourceProcessed(ResourceProcessedMessage message) {
        log.info("Processing resource processed event for resourceId={}", message.resourceId());
        resourceService.markProcessed(message.resourceId());
        log.info("Moved resource to permanent storage for resourceId={}", message.resourceId());
    }

    public record ResourceProcessedMessage(Long resourceId) {
    }
}
