package org.acssz.ezclaim.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(name = "PhotoCreateRequest", description = "Create a Photo record after uploading")
public class PhotoCreateRequest {
    @Schema(description = "Bucket name (optional)")
    private String bucket; // optional; defaults to app.objectstore.bucket
    @NotBlank
    @Schema(description = "Uploaded object key", example = "photos/2025/08/uuid.jpg")
    private String key;
}
