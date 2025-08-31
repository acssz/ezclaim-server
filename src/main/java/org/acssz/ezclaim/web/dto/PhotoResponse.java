package org.acssz.ezclaim.web.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PhotoResponse {
    String id;
    String bucket;
    String key;
    Instant uploadedAt;
}

