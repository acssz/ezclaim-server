package org.acssz.ezclaim.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.acssz.ezclaim.domain.Claim;
import org.acssz.ezclaim.domain.ClaimStatus;
import org.acssz.ezclaim.domain.Photo;
import org.acssz.ezclaim.domain.Tag;
import org.acssz.ezclaim.repository.ClaimRepository;
import org.acssz.ezclaim.repository.PhotoRepository;
import org.acssz.ezclaim.repository.TagRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimService {
    private final ClaimRepository repository;
    private final PhotoRepository photoRepository;
    private final TagRepository tagRepository;

    public List<Claim> findAll() {
        return repository.findAll();
    }

    public Claim findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + id));
    }

    public Claim create(String title, String description, ClaimStatus status,
                        List<String> photoIds, List<String> tagIds) {
        Instant now = Instant.now();
        Claim claim = Claim.builder()
                .title(title)
                .description(description)
                .status(status != null ? status : ClaimStatus.NEW)
                .createdAt(now)
                .updatedAt(now)
                .photos(resolvePhotos(photoIds))
                .tags(resolveTags(tagIds))
                .build();
        Claim saved = repository.save(claim);
        log.info("Created claim {}", saved.getId());
        return saved;
    }

    public Claim update(String id, String title, String description, ClaimStatus status,
                        List<String> photoIds, List<String> tagIds) {
        Claim existing = findById(id);
        existing.setTitle(title);
        existing.setDescription(description);
        if (status != null) existing.setStatus(status);
        if (photoIds != null) existing.setPhotos(resolvePhotos(photoIds));
        if (tagIds != null) existing.setTags(resolveTags(tagIds));
        existing.setUpdatedAt(Instant.now());
        return repository.save(existing);
    }

    public void delete(String id) {
        Claim existing = findById(id);
        repository.delete(existing);
        log.info("Deleted claim {}", id);
    }

    private List<Photo> resolvePhotos(List<String> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<Photo> found = photoRepository.findAllById(ids);
        ensureAllFound(ids, found.stream().map(Photo::getId).filter(Objects::nonNull).toList(), "photo");
        return found;
    }

    private List<Tag> resolveTags(List<String> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<Tag> found = tagRepository.findAllById(ids);
        ensureAllFound(ids, found.stream().map(Tag::getId).filter(Objects::nonNull).toList(), "tag");
        return found;
    }

    private void ensureAllFound(List<String> requested, List<String> found, String type) {
        Set<String> missing = new HashSet<>(requested);
        missing.removeAll(new HashSet<>(found));
        if (!missing.isEmpty()) {
            throw new ResourceNotFoundException("Unknown " + type + " id(s): " + String.join(",", missing));
        }
    }
}
