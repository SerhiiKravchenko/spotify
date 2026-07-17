package org.epam.learn.resourceprocessor.messaging;

import org.epam.learn.resourceprocessor.client.ResourceServiceClient;
import org.epam.learn.resourceprocessor.client.SongServiceClient;
import org.epam.learn.resourceprocessor.model.SongMetadata;
import org.epam.learn.resourceprocessor.service.MetadataExtractorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ResourceMessageListener {

    private static final Logger log = LoggerFactory.getLogger(ResourceMessageListener.class);

    private final ResourceServiceClient resourceServiceClient;
    private final MetadataExtractorService metadataExtractorService;
    private final SongServiceClient songServiceClient;

    public ResourceMessageListener(ResourceServiceClient resourceServiceClient,
                                   MetadataExtractorService metadataExtractorService,
                                   SongServiceClient songServiceClient) {
        this.resourceServiceClient = resourceServiceClient;
        this.metadataExtractorService = metadataExtractorService;
        this.songServiceClient = songServiceClient;
    }

    @RabbitListener(queues = "${rabbitmq.queue}")
    public void handleResourceUploaded(ResourceUploadedMessage message) {
        log.info("Processing resource upload event for resourceId={}", message.resourceId());

        byte[] resourceData = resourceServiceClient.getResource(message.resourceId());
        SongMetadata metadata = metadataExtractorService.extract(resourceData);
        metadata.setId(message.resourceId());
        songServiceClient.saveSongMetadata(metadata);

        log.info("Saved song metadata for resourceId={}", message.resourceId());
    }

    public record ResourceUploadedMessage(Long resourceId) {}
}
