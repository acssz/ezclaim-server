package org.acssz.ezclaim.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "photos")
public class Photo {
    @Id
    private String id;

    private String bucket;
    private String key;
    private Instant uploadedAt;
}

