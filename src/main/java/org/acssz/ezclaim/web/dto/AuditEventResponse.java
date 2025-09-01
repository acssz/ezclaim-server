package org.acssz.ezclaim.web.dto;

import java.time.Instant;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditEventResponse {
    private String id;
    private String entityType;
    private String entityId;
    private String action;
    private Instant occurredAt;
    private Map<String, Object> data;
}

