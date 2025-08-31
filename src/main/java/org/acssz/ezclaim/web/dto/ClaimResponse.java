package org.acssz.ezclaim.web.dto;

import java.time.Instant;
import java.util.List;

import org.acssz.ezclaim.domain.ClaimStatus;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ClaimResponse {
    String id;
    String title;
    String description;
    ClaimStatus status;
    Instant createdAt;
    Instant updatedAt;

    List<PhotoResponse> photos;
    List<TagResponse> tags;
}
