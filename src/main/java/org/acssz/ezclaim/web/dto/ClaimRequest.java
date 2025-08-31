package org.acssz.ezclaim.web.dto;

import java.util.List;

import org.acssz.ezclaim.domain.ClaimStatus;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClaimRequest {
    @NotBlank
    private String title;

    private String description; // optional

    private ClaimStatus status; // optional; defaults to NEW if null

    // Optional references by id, if you want to attach existing photos/tags
    private List<String> photoIds;
    private List<String> tagIds;
}
