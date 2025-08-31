package org.acssz.ezclaim.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TagResponse {
    String id;
    String label;
    String color;
}

