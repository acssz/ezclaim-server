package org.acssz.ezclaim.service;

import java.util.List;

import org.acssz.ezclaim.domain.Tag;
import org.acssz.ezclaim.repository.TagRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository repository;

    public List<Tag> list() { return repository.findAll(); }
    public Tag get(String id) { return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tag not found: " + id)); }
    public Tag create(String label, String color) { return repository.save(Tag.builder().label(label).color(color).build()); }
    public Tag update(String id, String label, String color) {
        Tag t = get(id);
        t.setLabel(label);
        t.setColor(color);
        return repository.save(t);
    }
    public void delete(String id) { repository.deleteById(id); }
}

