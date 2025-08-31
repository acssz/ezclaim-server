package org.acssz.ezclaim.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.objectstore")
public class ObjectStoreProperties {
    private String endpoint;        // Optional: set for S3-compatible (e.g., MinIO)
    private String region = "us-east-1"; // Default region
    private String accessKey;
    private String secretKey;
    private String bucket;
    private Boolean pathStyle;      // If null, auto-enable when endpoint is set
    private Boolean ensureBucket;   // Control bucket creation on startup

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }

    public Boolean getPathStyle() { return pathStyle; }
    public void setPathStyle(Boolean pathStyle) { this.pathStyle = pathStyle; }

    public Boolean getEnsureBucket() { return ensureBucket; }
    public void setEnsureBucket(Boolean ensureBucket) { this.ensureBucket = ensureBucket; }
}

