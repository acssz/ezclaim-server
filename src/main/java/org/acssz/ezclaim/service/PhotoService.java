package org.acssz.ezclaim.service;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.acssz.ezclaim.config.ObjectStoreProperties;
import org.acssz.ezclaim.domain.Photo;
import org.acssz.ezclaim.repository.PhotoRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoService {
    private final PhotoRepository photoRepository;
    private final S3Client s3;
    private final S3Presigner presigner;
    private final ObjectStoreProperties props;

    public List<Photo> list() { return photoRepository.findAll(); }

    public Photo get(String id) {
        return photoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found: " + id));
    }

    public Photo createRecord(String bucket, String key) {
        String b = bucket != null && !bucket.isBlank() ? bucket : props.getBucket();
        if (b == null || b.isBlank()) throw new IllegalArgumentException("Bucket is required");
        Photo p = Photo.builder()
                .bucket(b)
                .key(key)
                .uploadedAt(Instant.now())
                .build();
        return photoRepository.save(p);
    }

    public void delete(String id, boolean deleteObject) {
        Photo p = get(id);
        photoRepository.deleteById(id);
        if (deleteObject) {
            try {
                s3.deleteObject(DeleteObjectRequest.builder().bucket(p.getBucket()).key(p.getKey()).build());
            } catch (S3Exception e) {
                log.warn("Failed to delete object from S3: {}/{} - {}", p.getBucket(), p.getKey(), e.getMessage());
            }
        }
    }

    public PresignedPut signUpload(String bucket, String key, String contentType, Duration expiresIn) {
        String b = bucket != null && !bucket.isBlank() ? bucket : props.getBucket();
        if (b == null || b.isBlank()) throw new IllegalArgumentException("Bucket is required");
        String k = key != null && !key.isBlank() ? key : UUID.randomUUID().toString();

        PutObjectRequest.Builder put = PutObjectRequest.builder()
                .bucket(b)
                .key(k);
        if (contentType != null && !contentType.isBlank()) put.contentType(contentType);

        PutObjectPresignRequest req = PutObjectPresignRequest.builder()
                .signatureDuration(expiresIn != null ? expiresIn : Duration.ofMinutes(15))
                .putObjectRequest(put.build())
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(req);

        return new PresignedPut(
                b,
                k,
                presigned.url(),
                presigned.httpRequest().headers(),
                Instant.now().plus(req.signatureDuration())
        );
    }

    public PresignedGet signDownload(String bucket, String key, Duration expiresIn) {
        String b = bucket != null && !bucket.isBlank() ? bucket : props.getBucket();
        if (b == null || b.isBlank()) throw new IllegalArgumentException("Bucket is required");

        // optional: verify object exists
        try {
            s3.headObject(HeadObjectRequest.builder().bucket(b).key(key).build());
        } catch (NoSuchKeyException e) {
            throw new ResourceNotFoundException("Object not found: " + b + "/" + key);
        } catch (S3Exception e) {
            log.debug("headObject failed: {}", e.getMessage());
        }

        GetObjectPresignRequest req = GetObjectPresignRequest.builder()
                .signatureDuration(expiresIn != null ? expiresIn : Duration.ofMinutes(15))
                .getObjectRequest(b1 -> b1.bucket(b).key(key))
                .build();

        PresignedGetObjectRequest presigned = presigner.presignGetObject(req);
        return new PresignedGet(presigned.url(), Instant.now().plus(req.signatureDuration()));
    }

    // Simple records for responses
    public record PresignedPut(String bucket, String key, URL url, Map<String, List<String>> headers, Instant expiresAt) {}
    public record PresignedGet(URL url, Instant expiresAt) {}
}
