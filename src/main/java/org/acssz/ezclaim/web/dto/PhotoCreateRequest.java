package org.acssz.ezclaim.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PhotoCreateRequest {
    private String bucket; // optional; defaults to app.objectstore.bucket
    @NotBlank
    private String key;
}

