package org.acssz.ezclaim.web;

import java.util.List;
import java.util.stream.Collectors;

import org.acssz.ezclaim.domain.Claim;
import org.acssz.ezclaim.domain.Photo;
import org.acssz.ezclaim.domain.Tag;
import org.acssz.ezclaim.service.ClaimService;
import org.acssz.ezclaim.web.dto.ClaimRequest;
import org.acssz.ezclaim.web.dto.ClaimResponse;
import org.acssz.ezclaim.web.dto.PhotoResponse;
import org.acssz.ezclaim.web.dto.TagResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Validated
public class ClaimController {

    private final ClaimService service;

    @GetMapping
    public List<ClaimResponse> list() {
        return service.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ClaimResponse get(@PathVariable String id) {
        return toResponse(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ClaimResponse> create(@Valid @RequestBody ClaimRequest req) {
        Claim created = service.create(req.getTitle(), req.getDescription(), req.getStatus(),
                req.getPhotoIds(), req.getTagIds());
        return ResponseEntity.ok(toResponse(created));
    }

    @PutMapping("/{id}")
    public ClaimResponse update(@PathVariable String id, @Valid @RequestBody ClaimRequest req) {
        return toResponse(service.update(id, req.getTitle(), req.getDescription(), req.getStatus(),
                req.getPhotoIds(), req.getTagIds()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ClaimResponse toResponse(Claim c) {
        return ClaimResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .photos(c.getPhotos() == null ? List.of() : c.getPhotos().stream().map(this::toResponse).collect(Collectors.toList()))
                .tags(c.getTags() == null ? List.of() : c.getTags().stream().map(this::toResponse).collect(Collectors.toList()))
                .build();
    }

    private PhotoResponse toResponse(Photo p) {
        return PhotoResponse.builder()
                .id(p.getId())
                .bucket(p.getBucket())
                .key(p.getKey())
                .uploadedAt(p.getUploadedAt())
                .build();
    }

    private TagResponse toResponse(Tag t) {
        return TagResponse.builder()
                .id(t.getId())
                .label(t.getLabel())
                .color(t.getColor())
                .build();
    }
}
