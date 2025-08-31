package org.acssz.ezclaim.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagRequest {
    @NotBlank
    private String label;
    @NotBlank
    private String color;
}

