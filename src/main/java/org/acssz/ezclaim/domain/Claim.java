package org.acssz.ezclaim.domain;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "claims")
public class Claim {
    @Id
    private String id;

    private String title;
    private String description; // optional

    private ClaimStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    // Reference photos and tags stored in their own collections
    @DocumentReference(lazy = true)
    private List<Photo> photos;

    @DocumentReference(lazy = true)
    private List<Tag> tags;
}
