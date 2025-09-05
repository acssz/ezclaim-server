package org.acssz.ezclaim.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.acssz.ezclaim.domain.Tag;
import org.acssz.ezclaim.service.TagService;
import org.acssz.ezclaim.web.dto.TagRequest;
import org.acssz.ezclaim.web.dto.TagResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Validated
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "Manage tags/labels")
public class TagController {
  private final TagService service;

  @GetMapping
  @Operation(summary = "List tags", description = "Public access")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "OK")})
  public List<TagResponse> list() {
    return service.list().stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get tag by id", description = "Public access")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found")
  })
  public TagResponse get(@PathVariable String id) {
    return toResponse(service.get(id));
  }

  @PostMapping
  @Operation(summary = "Create tag", description = "Requires TAG_WRITE scope")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Created"),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
  })
  @SecurityRequirement(name = "bearerAuth")
  public TagResponse create(@Valid @RequestBody TagRequest req) {
    return toResponse(service.create(req.getLabel(), req.getColor()));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update tag", description = "Requires TAG_WRITE scope")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Updated"),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
    @ApiResponse(responseCode = "404", description = "Not found")
  })
  @SecurityRequirement(name = "bearerAuth")
  public TagResponse update(@PathVariable String id, @Valid @RequestBody TagRequest req) {
    return toResponse(service.update(id, req.getLabel(), req.getColor()));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete tag", description = "Requires TAG_WRITE scope")
  @ApiResponses({
    @ApiResponse(
        responseCode = "204",
        description = "Deleted",
        content = @Content(schema = @Schema(hidden = true))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
    @ApiResponse(responseCode = "404", description = "Not found")
  })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  private TagResponse toResponse(Tag t) {
    return TagResponse.builder().id(t.getId()).label(t.getLabel()).color(t.getColor()).build();
  }
}
