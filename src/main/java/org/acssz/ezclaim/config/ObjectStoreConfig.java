package org.acssz.ezclaim.config;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.S3Presigner.Builder;

@Configuration
@EnableConfigurationProperties(ObjectStoreProperties.class)
@Slf4j
public class ObjectStoreConfig {

  @Bean
  public S3Client s3Client(ObjectStoreProperties props) {
    S3ClientBuilder builder = S3Client.builder();

    Region region = Region.of(props.getRegion() != null ? props.getRegion() : "us-east-1");
    builder.region(region);

    AwsCredentialsProvider credsProvider;
    if (props.getAccessKey() != null && props.getSecretKey() != null) {
      credsProvider =
          StaticCredentialsProvider.create(
              AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey()));
    } else {
      credsProvider = DefaultCredentialsProvider.create();
    }
    builder.credentialsProvider(credsProvider);

    if (props.getEndpoint() != null && !props.getEndpoint().isBlank()) {
      builder.endpointOverride(URI.create(props.getEndpoint()));
    }

    boolean pathStyle =
        props.getPathStyle() != null
            ? props.getPathStyle()
            : (props.getEndpoint() != null); // default true when using custom endpoint

    builder.serviceConfiguration(
        S3Configuration.builder().pathStyleAccessEnabled(pathStyle).build());

    return builder.build();
  }

  @Bean
  public S3Presigner s3Presigner(ObjectStoreProperties props) {
    Builder builder = S3Presigner.builder();
    Region region = Region.of(props.getRegion() != null ? props.getRegion() : "us-east-1");
    builder.region(region);

    AwsCredentialsProvider credsProvider;
    if (props.getAccessKey() != null && props.getSecretKey() != null) {
      credsProvider =
          StaticCredentialsProvider.create(
              AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey()));
    } else {
      credsProvider = DefaultCredentialsProvider.create();
    }
    builder.credentialsProvider(credsProvider);

    if (props.getEndpoint() != null && !props.getEndpoint().isBlank()) {
      builder.endpointOverride(URI.create(props.getEndpoint()));
    }

    boolean pathStyle =
        props.getPathStyle() != null ? props.getPathStyle() : (props.getEndpoint() != null);

    builder.serviceConfiguration(
        S3Configuration.builder().pathStyleAccessEnabled(pathStyle).build());

    return builder.build();
  }

  @Bean
  @ConditionalOnProperty(
      name = "app.objectstore.ensure-bucket",
      havingValue = "true",
      matchIfMissing = true)
  public CommandLineRunner ensureBucket(S3Client s3, ObjectStoreProperties props) {
    return _ -> {
      String bucket = props.getBucket();
      if (bucket == null || bucket.isBlank()) {
        log.warn("Object store bucket not set; skip bucket creation.");
        return;
      }
      try {
        s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        log.info("Object store bucket exists: {}", bucket);
      } catch (S3Exception ex) {
        if (ex.statusCode() == 404) {
          CreateBucketRequest.Builder req = CreateBucketRequest.builder().bucket(bucket);
          if (props.getEndpoint() == null || props.getEndpoint().isBlank()) {
            req.createBucketConfiguration(
                CreateBucketConfiguration.builder().locationConstraint(props.getRegion()).build());
          }
          s3.createBucket(req.build());
          log.info("Created object store bucket: {}", bucket);
        } else {
          log.warn("Bucket check failed (status {}): {}", ex.statusCode(), ex.getMessage());
        }
      } catch (Exception e) {
        log.warn("Bucket check/creation failed: {}", e.getMessage());
      }
    };
  }
}
