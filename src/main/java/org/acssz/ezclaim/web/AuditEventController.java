package org.acssz.ezclaim.web;

import java.time.Instant;

import org.acssz.ezclaim.audit.AuditEvent;
import org.acssz.ezclaim.service.AuditEventService;
import org.acssz.ezclaim.web.dto.AuditEventResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;

@RestController
@RequestMapping("/api/audit-events")
@RequiredArgsConstructor
@Validated
@Tag(name = "Audit Events", description = "Search and read audit events")
public class AuditEventController {
    private final AuditEventService service;

    @GetMapping
    @Operation(summary = "Search audit events", description = "Requires AUDIT scope")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    public Page<AuditEventResponse> list(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "occurredAt,desc") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<AuditEvent> result = service.search(entityType, entityId, action, from, to, pageable);
        return result.map(this::toResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get audit event", description = "Requires AUDIT scope")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public AuditEventResponse get(@PathVariable String id) {
        return toResponse(service.getById(id));
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) return Sort.by(Sort.Direction.DESC, "occurredAt");
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

    private AuditEventResponse toResponse(AuditEvent e) {
        return AuditEventResponse.builder()
                .id(e.getId())
                .entityType(e.getEntityType())
                .entityId(e.getEntityId())
                .action(e.getAction())
                .occurredAt(e.getOccurredAt())
                .data(e.getData())
                .build();
    }
}
