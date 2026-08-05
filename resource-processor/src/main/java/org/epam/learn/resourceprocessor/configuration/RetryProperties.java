package org.epam.learn.resourceprocessor.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "retry")
public class RetryProperties {

    private int maxRetries = 3;
    private long delayMillis = 3000;
    private double multiplier = 2;
    private long maxDelayMillis = 5000;
    private long jitterMillis = 50;
    private long timeoutSeconds = 30;

    public int getMaxRetries() {
        return maxRetries;
    }

    public long getDelayMillis() {
        return delayMillis;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public long getMaxDelayMillis() {
        return maxDelayMillis;
    }

    public long getJitterMillis() {
        return jitterMillis;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void setDelayMillis(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public void setMaxDelayMillis(long maxDelayMillis) {
        this.maxDelayMillis = maxDelayMillis;
    }

    public void setJitterMillis(long jitterMillis) {
        this.jitterMillis = jitterMillis;
    }

    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
