package org.acssz.ezclaim.web;

import java.util.List;

import org.acssz.ezclaim.domain.Tag;
import org.acssz.ezclaim.service.TagService;
import org.acssz.ezclaim.web.dto.TagRequest;
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
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Validated
public class TagController {
    private final TagService service;

    @GetMapping
    public List<TagResponse> list() { return service.list().stream().map(this::toResponse).toList(); }

    @GetMapping("/{id}")
    public TagResponse get(@PathVariable String id) { return toResponse(service.get(id)); }

    @PostMapping
    public TagResponse create(@Valid @RequestBody TagRequest req) { return toResponse(service.create(req.getLabel(), req.getColor())); }

    @PutMapping("/{id}")
    public TagResponse update(@PathVariable String id, @Valid @RequestBody TagRequest req) { return toResponse(service.update(id, req.getLabel(), req.getColor())); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) { service.delete(id); return ResponseEntity.noContent().build(); }

    private TagResponse toResponse(Tag t) { return TagResponse.builder().id(t.getId()).label(t.getLabel()).color(t.getColor()).build(); }
}

