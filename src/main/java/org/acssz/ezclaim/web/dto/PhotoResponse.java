package org.acssz.ezclaim.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(name = "PhotoResponse")
public class PhotoResponse {
  @Schema(example = "6650eac3bfa5e7a3e9d1a2bc")
  String id;

  @Schema(example = "ezclaim-dev")
  String bucket;

  @Schema(example = "photos/2025/08/uuid.jpg")
  String key;

  @Schema(example = "2025-08-12T10:00:00Z")
  Instant uploadedAt;
}
