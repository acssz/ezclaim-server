package org.acssz.ezclaim.audit;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "prod"})
public class MongoChangePublisher extends AbstractMongoEventListener<Object> {
  public static final String AUDIT_COLLECTION = "audit_events";
  private static final String OUT_BINDING = "auditEvents-out-0";

  private final StreamBridge streamBridge;

  @Override
  public void onAfterSave(AfterSaveEvent<Object> event) {
    if (AUDIT_COLLECTION.equals(event.getCollectionName())) return; // avoid loop
    Document doc = event.getDocument();
    if (doc == null) return;
    String entityId = doc.get("_id") != null ? doc.get("_id").toString() : null;
    String entityType =
        event.getSource() != null ? event.getSource().getClass().getName() : "unknown";
    Map<String, Object> data = new HashMap<>(doc);
    data.remove("_class");

    AuditEvent ae =
        AuditEvent.builder()
            .entityType(entityType)
            .entityId(entityId)
            .action("SAVE")
            .occurredAt(Instant.now())
            .data(data)
            .build();
    boolean sent = streamBridge.send(OUT_BINDING, ae);
    if (!sent) {
      log.warn("Failed to send audit SAVE event for {}:{}", entityType, entityId);
    } else {
      log.debug("Published audit SAVE event for {}:{}", entityType, entityId);
    }
  }

  @Override
  public void onAfterDelete(AfterDeleteEvent<Object> event) {
    if (AUDIT_COLLECTION.equals(event.getCollectionName())) return; // avoid loop
    Document doc = event.getDocument();
    String entityId = doc != null && doc.get("_id") != null ? doc.get("_id").toString() : null;
    String entityType;
    if (doc != null && doc.get("_class") instanceof String s && !s.isBlank()) {
      entityType = s;
    } else {
      entityType = "collection:" + event.getCollectionName();
    }

    AuditEvent ae =
        AuditEvent.builder()
            .entityType(entityType)
            .entityId(entityId)
            .action("DELETE")
            .occurredAt(Instant.now())
            .data(null)
            .build();
    boolean sent = streamBridge.send(OUT_BINDING, ae);
    if (!sent) {
      log.warn("Failed to send audit DELETE event for {}:{}", entityType, entityId);
    } else {
      log.debug("Published audit DELETE event for {}:{}", entityType, entityId);
    }
  }
}
