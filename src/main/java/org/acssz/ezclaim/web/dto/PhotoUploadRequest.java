package org.acssz.ezclaim.web.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(name = "PhotoUploadRequest", description = "Request to presign an upload")
public class PhotoUploadRequest {
    @Schema(description = "Bucket name (optional)")
    private String bucket;        // optional; defaults to app.objectstore.bucket
    @Schema(description = "Object key (optional; UUID if empty)")
    private String key;           // optional; generate UUID if empty
    @Schema(description = "Content-Type, e.g. image/jpeg")
    private String contentType;   // optional
    @Positive
    @Schema(description = "URL expiry in seconds", example = "900")
    private Integer expiresInSeconds; // optional; default 900
}
