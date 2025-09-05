package org.acssz.ezclaim.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import org.acssz.ezclaim.audit.AuditEvent;
import org.acssz.ezclaim.service.AuditEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuditEventController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class AuditEventControllerTest {

  @Autowired MockMvc mvc;
  @Autowired AuditEventService service;

  @TestConfiguration
  static class Cfg {
    @Bean
    AuditEventService auditEventService() {
      return org.mockito.Mockito.mock(AuditEventService.class);
    }
  }

  @BeforeEach
  void resetMocks() {
    org.mockito.Mockito.reset(service);
  }

  @Test
  void list_returns_page_with_content() throws Exception {
    AuditEvent a1 =
        AuditEvent.builder()
            .id("a1")
            .entityType("T")
            .entityId("1")
            .action("SAVE")
            .occurredAt(Instant.parse("2025-01-01T00:00:00Z"))
            .build();
    AuditEvent a2 =
        AuditEvent.builder()
            .id("a2")
            .entityType("T")
            .entityId("2")
            .action("DELETE")
            .occurredAt(Instant.parse("2025-01-02T00:00:00Z"))
            .build();
    Page<AuditEvent> page =
        new PageImpl<>(
            List.of(a1, a2), PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "occurredAt")), 2);
    when(service.search(isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
        .thenReturn(page);

    mvc.perform(get("/api/audit-events").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.content[0].id").value("a1"))
        .andExpect(jsonPath("$.content[1].action").value("DELETE"));
  }

  @Test
  void list_parses_sort_parameter() throws Exception {
    Page<AuditEvent> empty = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
    when(service.search(isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
        .thenReturn(empty);

    mvc.perform(
            get("/api/audit-events")
                .param("sort", "entityType,asc")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(service, atLeastOnce())
        .search(isNull(), isNull(), isNull(), isNull(), isNull(), captor.capture());
    Pageable p = captor.getValue();
    Sort.Order order = p.getSort().getOrderFor("entityType");
    assertThat(order).isNotNull();
    assertThat(order.isAscending()).isTrue();
  }

  @Test
  void list_parses_time_range_parameters() throws Exception {
    Page<AuditEvent> empty = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
    when(service.search(
            isNull(),
            isNull(),
            isNull(),
            any(Instant.class),
            any(Instant.class),
            any(Pageable.class)))
        .thenReturn(empty);

    String from = "2025-01-01T00:00:00Z";
    String to = "2025-01-02T23:59:59Z";
    mvc.perform(
            get("/api/audit-events")
                .param("from", from)
                .param("to", to)
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    ArgumentCaptor<Instant> fromCap = ArgumentCaptor.forClass(Instant.class);
    ArgumentCaptor<Instant> toCap = ArgumentCaptor.forClass(Instant.class);
    verify(service)
        .search(
            isNull(), isNull(), isNull(), fromCap.capture(), toCap.capture(), any(Pageable.class));
    assertThat(fromCap.getValue()).isEqualTo(Instant.parse(from));
    assertThat(toCap.getValue()).isEqualTo(Instant.parse(to));
  }

  @Test
  void get_by_id_returns_entity() throws Exception {
    AuditEvent a1 =
        AuditEvent.builder()
            .id("a1")
            .entityType("T")
            .entityId("1")
            .action("SAVE")
            .occurredAt(Instant.parse("2025-01-01T00:00:00Z"))
            .build();
    when(service.getById("a1")).thenReturn(a1);

    mvc.perform(get("/api/audit-events/a1").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("a1"))
        .andExpect(jsonPath("$.entityId").value("1"))
        .andExpect(jsonPath("$.action").value("SAVE"));
  }
}
