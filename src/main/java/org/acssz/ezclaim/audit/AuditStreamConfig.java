package org.acssz.ezclaim.audit;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "prod"})
public class AuditStreamConfig {
  private final AuditEventRepository repository;

  @Bean
  public Consumer<AuditEvent> auditEvents() {
    return evt -> {
      try {
        repository.save(evt);
        log.debug(
            "Audit event persisted: {}:{} {}",
            evt.getEntityType(),
            evt.getEntityId(),
            evt.getAction());
      } catch (Exception e) {
        log.error("Failed to persist audit event", e);
      }
    };
  }
}
