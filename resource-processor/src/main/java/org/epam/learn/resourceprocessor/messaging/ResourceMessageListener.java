package org.epam.learn.resourceprocessor.messaging;

import com.rabbitmq.client.Channel;

import java.io.IOException;

import org.epam.learn.resourceprocessor.client.ResourceServiceClient;
import org.epam.learn.resourceprocessor.client.SongServiceClient;
import org.epam.learn.resourceprocessor.model.SongMetadata;
import org.epam.learn.resourceprocessor.service.MetadataExtractorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
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

    @RabbitListener(queues = "${rabbitmq.queue}", ackMode = "MANUAL")
    public void handleResourceUploaded(ResourceUploadedMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        log.info("Processing resource upload event for resourceId={}", message.resourceId());

        try {
            byte[] resourceData = resourceServiceClient.getResource(message.resourceId());
            SongMetadata metadata = metadataExtractorService.extract(resourceData);
            metadata.setId(message.resourceId());
            songServiceClient.saveSongMetadata(metadata);

            channel.basicAck(tag, false);

            log.info("Saved song metadata for resourceId={}", message.resourceId());
        } catch (IOException e) {
            try {
                channel.basicReject(tag, false);
            } catch (IOException ex) {
                log.error("Failed to nack message for resourceId={}", message.resourceId(), ex);
            }
            throw new RuntimeException(e);
        }
    }

    public record ResourceUploadedMessage(Long resourceId) {
    }
}
