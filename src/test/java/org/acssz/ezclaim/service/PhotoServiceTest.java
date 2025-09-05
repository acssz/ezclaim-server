package org.acssz.ezclaim.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import org.acssz.ezclaim.config.ObjectStoreProperties;
import org.acssz.ezclaim.domain.Photo;
import org.acssz.ezclaim.repository.PhotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

  @Mock PhotoRepository photoRepository;
  @Mock S3Client s3Client;
  @Mock S3Presigner s3Presigner;

  ObjectStoreProperties props;

  @InjectMocks PhotoService photoService;

  @BeforeEach
  void setup() {
    props = new ObjectStoreProperties();
    props.setBucket("bkt");
    // re-inject since @InjectMocks happens before @BeforeEach
    photoService = new PhotoService(photoRepository, s3Client, s3Presigner, props);
  }

  @Test
  void createRecord_sets_defaults_and_saves() {
    when(photoRepository.save(any(Photo.class)))
        .thenAnswer(
            inv -> {
              Photo p = inv.getArgument(0);
              p.setId("p1");
              return p;
            });
    Photo p = photoService.createRecord(null, "key");
    assertThat(p.getId()).isEqualTo("p1");
    assertThat(p.getBucket()).isEqualTo("bkt");
    assertThat(p.getKey()).isEqualTo("key");
    assertThat(p.getUploadedAt()).isNotNull();
  }

  @Test
  void delete_with_deleteObject_true_calls_s3() {
    Photo p = Photo.builder().id("p1").bucket("b").key("k").uploadedAt(Instant.now()).build();
    when(photoRepository.findById("p1")).thenReturn(java.util.Optional.of(p));

    photoService.delete("p1", true);

    verify(photoRepository, times(1)).deleteById("p1");
    verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
  }

  @Test
  void signUpload_returns_presigned_put_details() throws Exception {
    PresignedPutObjectRequest presigned = mock(PresignedPutObjectRequest.class);
    when(presigned.url()).thenReturn(new URL("http://localhost:9000/put"));
    SdkHttpFullRequest httpReq =
        SdkHttpFullRequest.builder()
            .method(SdkHttpMethod.PUT)
            .host("localhost")
            .protocol("http")
            .encodedPath("/put")
            .build();
    when(presigned.httpRequest()).thenReturn(httpReq);
    when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presigned);

    var result = photoService.signUpload(null, null, "image/jpeg", Duration.ofSeconds(60));
    assertThat(result.bucket()).isEqualTo("bkt");
    assertThat(result.key()).isNotBlank();
    assertThat(result.url().toString()).contains("http://localhost:9000/put");
  }

  @Test
  void signDownload_returns_presigned_get_details() throws Exception {
    PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
    when(presigned.url()).thenReturn(new URL("http://localhost:9000/get"));
    when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

    var result = photoService.signDownload("bkt", "key", Duration.ofSeconds(60));
    assertThat(result.url().toString()).contains("http://localhost:9000/get");
  }
}
