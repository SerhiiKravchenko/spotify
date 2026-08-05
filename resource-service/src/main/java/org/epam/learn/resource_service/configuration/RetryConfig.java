package org.epam.learn.resource_service.configuration;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.web.client.RestClientException;

@Configuration
@EnableConfigurationProperties(RetryProperties.class)
public class RetryConfig {

    @Bean
    @RefreshScope
    public RetryTemplate customRetryTemplate(RetryProperties properties) {
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .includes(RestClientException.class)
                .maxRetries(properties.getMaxRetries())
                .delay(Duration.ofMillis(properties.getDelayMillis()))
                .multiplier(properties.getMultiplier())
                .maxDelay(Duration.ofMillis(properties.getMaxDelayMillis()))
                .jitter(Duration.ofMillis(properties.getJitterMillis()))
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .build();

        return new RetryTemplate(retryPolicy);
    }
}
