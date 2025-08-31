package org.acssz.ezclaim.web.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PhotoUploadRequest {
    private String bucket;        // optional; defaults to app.objectstore.bucket
    private String key;           // optional; generate UUID if empty
    private String contentType;   // optional
    @Positive
    private Integer expiresInSeconds; // optional; default 900
}

