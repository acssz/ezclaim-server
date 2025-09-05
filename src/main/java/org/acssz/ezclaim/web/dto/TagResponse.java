package org.acssz.ezclaim.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(name = "TagResponse")
public class TagResponse {
  @Schema(example = "6650ee08cdb9303e0b73b0a2")
  String id;

  @Schema(example = "Travel")
  String label;

  @Schema(example = "#0ea5e9")
  String color;
}
