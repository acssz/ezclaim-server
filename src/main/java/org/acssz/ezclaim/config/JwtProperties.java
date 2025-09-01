package org.acssz.ezclaim.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {
    /** Secret for HS256 signing. In prod, provide via env. */
    private String secret;
    /** Token time-to-live. Default PT12H. */
    private Duration ttl = Duration.ofHours(12);
    /** Algorithm id. Currently only HS256 is supported. */
    private String algorithm = "HS256";

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public Duration getTtl() { return ttl; }
    public void setTtl(Duration ttl) { this.ttl = ttl; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
}

