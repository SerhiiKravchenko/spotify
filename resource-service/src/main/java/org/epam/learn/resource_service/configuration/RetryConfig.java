package org.epam.learn.resource_service.configuration;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.web.client.RestClientException;

@Configuration
public class RetryConfig {

    @Bean
    public RetryTemplate customRetryTemplate() {
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .includes(RestClientException.class)
                .maxRetries(3)
                .delay(Duration.ofMillis(3000))
                .multiplier(2)
                .maxDelay(Duration.ofSeconds(5))
                .jitter(Duration.ofMillis(50))
                .timeout(Duration.ofSeconds(30))
                .build();

        return new RetryTemplate(retryPolicy);
    }
}
