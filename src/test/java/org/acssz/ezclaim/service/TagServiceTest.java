package org.acssz.ezclaim.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.acssz.ezclaim.domain.Tag;
import org.acssz.ezclaim.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

  @Mock TagRepository tagRepository;
  @InjectMocks TagService tagService;

  @Test
  void create_returns_saved() {
    when(tagRepository.save(any(Tag.class)))
        .thenAnswer(
            inv -> {
              Tag t = inv.getArgument(0);
              t.setId("t1");
              return t;
            });
    Tag saved = tagService.create("label", "#fff");
    assertThat(saved.getId()).isEqualTo("t1");
    assertThat(saved.getLabel()).isEqualTo("label");
    assertThat(saved.getColor()).isEqualTo("#fff");
  }

  @Test
  void get_not_found_throws() {
    when(tagRepository.findById("nope")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> tagService.get("nope")).isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void update_changes_fields() {
    Tag existing = Tag.builder().id("t1").label("a").color("#000").build();
    when(tagRepository.findById("t1")).thenReturn(Optional.of(existing));
    when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));
    Tag updated = tagService.update("t1", "b", "#111");
    assertThat(updated.getLabel()).isEqualTo("b");
    assertThat(updated.getColor()).isEqualTo("#111");
  }
}
