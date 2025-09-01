package org.acssz.ezclaim.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(name = "TagRequest", description = "Create or update a tag")
public class TagRequest {
    @NotBlank
    @Schema(example = "Travel")
    private String label;
    @NotBlank
    @Schema(example = "#0ea5e9")
    private String color;
}
