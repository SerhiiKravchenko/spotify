package org.epam.learn.storage_service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.seed")
public class StorageSeedProperties {

    private String stagingType = "STAGING";
    private String stagingBucket = "staging-bucket";
    private String stagingPath = "/files";

    private String permanentType = "PERMANENT";
    private String permanentBucket = "permanent-bucket";
    private String permanentPath = "/files";

    public String getStagingType() {
        return stagingType;
    }

    public void setStagingType(String stagingType) {
        this.stagingType = stagingType;
    }

    public String getStagingBucket() {
        return stagingBucket;
    }

    public void setStagingBucket(String stagingBucket) {
        this.stagingBucket = stagingBucket;
    }

    public String getStagingPath() {
        return stagingPath;
    }

    public void setStagingPath(String stagingPath) {
        this.stagingPath = stagingPath;
    }

    public String getPermanentType() {
        return permanentType;
    }

    public void setPermanentType(String permanentType) {
        this.permanentType = permanentType;
    }

    public String getPermanentBucket() {
        return permanentBucket;
    }

    public void setPermanentBucket(String permanentBucket) {
        this.permanentBucket = permanentBucket;
    }

    public String getPermanentPath() {
        return permanentPath;
    }

    public void setPermanentPath(String permanentPath) {
        this.permanentPath = permanentPath;
    }
}
