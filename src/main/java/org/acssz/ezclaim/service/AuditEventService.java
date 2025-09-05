package org.acssz.ezclaim.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.acssz.ezclaim.audit.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditEventService {
  private final MongoTemplate template;

  public Page<AuditEvent> search(
      String entityType,
      String entityId,
      String action,
      Instant from,
      Instant to,
      Pageable pageable) {
    Query q = new Query();
    List<Criteria> criteria = new ArrayList<>();

    if (entityType != null && !entityType.isBlank())
      criteria.add(Criteria.where("entityType").is(entityType));
    if (entityId != null && !entityId.isBlank())
      criteria.add(Criteria.where("entityId").is(entityId));
    if (action != null && !action.isBlank()) criteria.add(Criteria.where("action").is(action));

    if (from != null || to != null) {
      Criteria time = Criteria.where("occurredAt");
      if (from != null) time = time.gte(from);
      if (to != null) time = time.lte(to);
      criteria.add(time);
    }

    if (!criteria.isEmpty()) {
      q.addCriteria(new Criteria().andOperator(criteria.toArray(Criteria[]::new)));
    }

    long total = template.count(q, AuditEvent.class);
    q.with(pageable);
    List<AuditEvent> content = template.find(q, AuditEvent.class);
    return new PageImpl<>(content, pageable, total);
  }

  public AuditEvent getById(String id) {
    AuditEvent evt = template.findById(id, AuditEvent.class);
    if (evt == null) throw new ResourceNotFoundException("AuditEvent not found: " + id);
    return evt;
  }
}
