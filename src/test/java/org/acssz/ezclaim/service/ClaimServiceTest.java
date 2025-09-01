package org.acssz.ezclaim.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.acssz.ezclaim.domain.Claim;
import org.acssz.ezclaim.domain.ClaimStatus;
import org.acssz.ezclaim.domain.Photo;
import org.acssz.ezclaim.domain.Tag;
import org.acssz.ezclaim.repository.ClaimRepository;
import org.acssz.ezclaim.repository.PhotoRepository;
import org.acssz.ezclaim.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock ClaimRepository claimRepository;
    @Mock PhotoRepository photoRepository;
    @Mock TagRepository tagRepository;

    @InjectMocks ClaimService claimService;

    Photo photo1;
    Tag tag1;

    @BeforeEach
    void setUp() {
        photo1 = Photo.builder().id("p1").bucket("b").key("k").uploadedAt(Instant.now()).build();
        tag1 = Tag.builder().id("t1").label("L").color("#fff").build();
    }

    @Test
    void create_claim_with_references() {
        when(photoRepository.findAllById(List.of("p1"))).thenReturn(List.of(photo1));
        when(tagRepository.findAllById(List.of("t1"))).thenReturn(List.of(tag1));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> {
            Claim c = inv.getArgument(0);
            c.setId("c1");
            return c;
        });

        Claim saved = claimService.create(
                "title", "desc", ClaimStatus.SUBMITTED,
                List.of("p1"), List.of("t1"),
                null, null, null, null,
                null, null,
                null);

        assertThat(saved.getId()).isEqualTo("c1");
        assertThat(saved.getPhotos()).extracting(Photo::getId).containsExactly("p1");
        assertThat(saved.getTags()).extracting(Tag::getId).containsExactly("t1");
        assertThat(saved.getStatus()).isEqualTo(ClaimStatus.SUBMITTED);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void create_claim_missing_photo_throws() {
        when(photoRepository.findAllById(List.of("missing"))).thenReturn(List.of());
        assertThatThrownBy(() -> claimService.create(
                "t", null, ClaimStatus.SUBMITTED,
                List.of("missing"), null,
                null, null, null, null,
                null, null,
                null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("photo id(s)");
    }

    @Test
    void update_claim_updates_fields() {
        Claim existing = Claim.builder().id("c1").title("old").description("d")
                .status(ClaimStatus.SUBMITTED).createdAt(Instant.now()).updatedAt(Instant.now()).build();
        when(claimRepository.findById("c1")).thenReturn(Optional.of(existing));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));

        Claim updated = claimService.update("c1", "new", "nd", ClaimStatus.SUBMITTED, null, null);
        assertThat(updated.getTitle()).isEqualTo("new");
        assertThat(updated.getDescription()).isEqualTo("nd");
        assertThat(updated.getStatus()).isEqualTo(ClaimStatus.SUBMITTED);
    }

    @Test
    void delete_claim_deletes_from_repo() {
        Claim existing = Claim.builder().id("c1").title("old").status(ClaimStatus.SUBMITTED).build();
        when(claimRepository.findById("c1")).thenReturn(Optional.of(existing));

        claimService.delete("c1");
        verify(claimRepository, times(1)).delete(eq(existing));
    }
}
