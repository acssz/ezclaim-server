package org.acssz.ezclaim.web;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.acssz.ezclaim.domain.Photo;
import org.acssz.ezclaim.service.PhotoService;
import org.acssz.ezclaim.web.dto.PhotoCreateRequest;
import org.acssz.ezclaim.web.dto.PhotoResponse;
import org.acssz.ezclaim.web.dto.PhotoUploadRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
@Validated
@Tag(name = "Photos", description = "Manage photo records and presigned URLs")
public class PhotoController {
    private final PhotoService service;

    @GetMapping
    @Operation(summary = "List photos", description = "Requires PHOTO_READ scope")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    public List<PhotoResponse> list() {
        return service.list().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get photo by id", description = "Public access")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    public PhotoResponse get(@PathVariable String id) { return toResponse(service.get(id)); }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete photo", description = "Requires PHOTO_DELETE scope; optionally delete underlying object.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> delete(@PathVariable String id, @RequestParam(defaultValue = "true") boolean deleteObject) {
        service.delete(id, deleteObject);
        return ResponseEntity.noContent().build();
    }

    // Step 1: client asks for a presigned PUT URL to upload a file
    @PostMapping("/presign-upload")
    @Operation(summary = "Presign upload", description = "Public: returns a presigned PUT URL and headers for upload.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<?> presignUpload(@Valid @RequestBody PhotoUploadRequest req) {
        var presigned = service.signUpload(req.getBucket(), req.getKey(), req.getContentType(),
                req.getExpiresInSeconds() != null ? Duration.ofSeconds(req.getExpiresInSeconds()) : null);
        return ResponseEntity.ok(Map.of(
                "bucket", presigned.bucket(),
                "key", presigned.key(),
                "url", presigned.url().toString(),
                "headers", presigned.headers(),
                "expiresAt", presigned.expiresAt().toString()
        ));
    }

    // Step 2: after a successful upload, create a Photo record (or do it in step 1 if you prefer)
    @PostMapping
    @Operation(summary = "Create photo record", description = "Public: create a record after uploading.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public PhotoResponse create(@Valid @RequestBody PhotoCreateRequest req) {
        Photo p = service.createRecord(req.getBucket(), req.getKey());
        return toResponse(p);
    }

    // Get a presigned GET URL to download the object
    @GetMapping("/{id}/download-url")
    @Operation(summary = "Presign download", description = "Public: returns a presigned GET URL to download the object.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<?> presignDownload(@PathVariable String id, @RequestParam(required = false) Integer expiresInSeconds) {
        Photo p = service.get(id);
        var signed = service.signDownload(p.getBucket(), p.getKey(),
                expiresInSeconds != null ? Duration.ofSeconds(expiresInSeconds) : null);
        return ResponseEntity.ok(Map.of(
                "url", signed.url().toString(),
                "expiresAt", signed.expiresAt().toString()
        ));
    }

    private PhotoResponse toResponse(Photo p) {
        return PhotoResponse.builder()
                .id(p.getId())
                .bucket(p.getBucket())
                .key(p.getKey())
                .uploadedAt(p.getUploadedAt())
                .build();
    }
}
