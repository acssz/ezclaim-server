package org.acssz.ezclaim.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.acssz.ezclaim.audit.AuditEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@ExtendWith(MockitoExtension.class)
class AuditEventServiceTest {

    @Mock MongoTemplate template;
    @InjectMocks AuditEventService service;

    @Test
    void search_returns_page_with_results() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "occurredAt"));
        AuditEvent a1 = AuditEvent.builder().id("a1").entityType("T").entityId("1").action("SAVE").occurredAt(Instant.now()).build();
        AuditEvent a2 = AuditEvent.builder().id("a2").entityType("T").entityId("2").action("DELETE").occurredAt(Instant.now()).build();

        when(template.count(any(Query.class), any(Class.class))).thenReturn(2L);
        when(template.find(any(Query.class), any(Class.class))).thenReturn(List.of(a1, a2));

        Page<AuditEvent> page = service.search(null, null, null, null, null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(AuditEvent::getId).containsExactly("a1", "a2");
        assertThat(page.getPageable().getSort().getOrderFor("occurredAt").isDescending()).isTrue();
    }

    @Test
    void getById_found_returns_entity() {
        AuditEvent a1 = AuditEvent.builder().id("a1").entityType("T").entityId("1").action("SAVE").occurredAt(Instant.now()).build();
        when(template.findById("a1", AuditEvent.class)).thenReturn(a1);
        AuditEvent found = service.getById("a1");
        assertThat(found.getId()).isEqualTo("a1");
    }

    @Test
    void getById_missing_throws() {
        when(template.findById("missing", AuditEvent.class)).thenReturn(null);
        assertThatThrownBy(() -> service.getById("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("AuditEvent not found");
    }
}

