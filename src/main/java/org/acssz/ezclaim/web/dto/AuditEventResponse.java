package org.acssz.ezclaim.web.dto;

import java.time.Instant;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@Schema(name = "AuditEventResponse")
public class AuditEventResponse {
    @Schema(example = "6650f1c0bca46a0c4cf9ee10")
    private String id;
    @Schema(example = "CLAIM")
    private String entityType;
    @Schema(example = "664a0c2f7b1f3c2d9b7c9a10")
    private String entityId;
    @Schema(example = "UPDATED")
    private String action;
    @Schema(example = "2025-08-12T10:05:00Z")
    private Instant occurredAt;
    @Schema(description = "Event details as key-value map")
    private Map<String, Object> data;
}
