package org.acssz.ezclaim.audit;

import java.time.Instant;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_events")
public class AuditEvent {
    @Id
    private String id;

    private String entityType;   // e.g., org.acssz.ezclaim.domain.Claim
    private String entityId;     // document id
    private String action;       // SAVE | DELETE | INSERT | UPDATE (optional granularity)
    private Instant occurredAt;  // event time
    private Map<String, Object> data; // snapshot or delta (here: snapshot of document)
}

