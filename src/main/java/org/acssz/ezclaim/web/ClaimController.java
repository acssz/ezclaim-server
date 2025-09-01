package org.acssz.ezclaim.web;

import java.util.List;
import java.util.stream.Collectors;

import org.acssz.ezclaim.domain.Claim;
import org.acssz.ezclaim.domain.Photo;
import org.acssz.ezclaim.domain.Tag;
import org.acssz.ezclaim.service.ClaimService;
import org.acssz.ezclaim.web.dto.ClaimRequest;
import org.acssz.ezclaim.web.dto.ClaimResponse;
import org.acssz.ezclaim.web.dto.ClaimPatchRequest;
import org.acssz.ezclaim.web.dto.PhotoResponse;
import org.acssz.ezclaim.web.dto.TagResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Validated
public class ClaimController {

    private final ClaimService service;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public List<ClaimResponse> list() {
        return service.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ClaimResponse get(@PathVariable String id,
                             @RequestParam(required = false) String password,
                             Authentication auth) {
        Claim c = service.findById(id);
        boolean hasPassword = c.getPasswordHash() != null && !c.getPasswordHash().isBlank();
        boolean privileged = auth != null && auth.isAuthenticated() && auth.getAuthorities() != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("SCOPE_CLAIM_READ") || a.equals("SCOPE_CLAIM_WRITE"));
        if (hasPassword && !privileged) {
            if (password == null || password.isBlank() || !passwordEncoder.matches(password, c.getPasswordHash())) {
                throw new org.springframework.security.access.AccessDeniedException("password required or invalid");
            }
        }
        return toResponse(c);
    }

    @PostMapping
    public ResponseEntity<ClaimResponse> create(@Valid @RequestBody ClaimRequest req) {
        Claim created = service.create(
                req.getTitle(), req.getDescription(), req.getStatus(),
                req.getPhotoIds(), req.getTagIds(),
                req.getAmount(), req.getCurrency(),
                toDomain(req.getPayout()), req.getRecipient(),
                req.getPassword(), passwordEncoder,
                req.getExpenseAt()
        );
        return ResponseEntity.ok(toResponse(created));
    }

    @PatchMapping("/{id}")
    public ClaimResponse patch(@PathVariable String id,
                               @RequestBody ClaimPatchRequest req,
                               Authentication auth) {
        Claim existing = service.findById(id);
        boolean privileged = auth != null && auth.isAuthenticated() && auth.getAuthorities() != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("SCOPE_CLAIM_WRITE"));
        boolean passwordOkOrNotSet = existing.getPasswordHash() == null
                || (req.getPassword() != null && passwordEncoder.matches(req.getPassword(), existing.getPasswordHash()));
        boolean allowUserStatusChange = !privileged && passwordOkOrNotSet;

        ClaimService.Patch patch = new ClaimService.Patch(
                req.getStatus(),
                req.getAmount(),
                req.getCurrency(),
                toDomain(req.getPayout()),
                req.getRecipient(),
                req.getTitle(),
                req.getDescription(),
                req.getExpenseAt()
        );
        Claim updated = service.patch(id, patch, privileged, allowUserStatusChange);
        return toResponse(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ClaimResponse toResponse(Claim c) {
        return ClaimResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .amount(c.getAmount())
                .currency(c.getCurrency())
                .recipient(c.getRecipient())
                .expenseAt(c.getExpenseAt())
                .payout(c.getPayout() == null ? null : ClaimResponse.PayoutInfo.builder()
                        .bankName(c.getPayout().getBankName())
                        .accountNumber(c.getPayout().getAccountNumber())
                        .iban(c.getPayout().getIban())
                        .swift(c.getPayout().getSwift())
                        .routingNumber(c.getPayout().getRoutingNumber())
                        .bankAddress(c.getPayout().getBankAddress())
                        .build())
                .photos(c.getPhotos() == null ? List.of() : c.getPhotos().stream().map(this::toResponse).collect(Collectors.toList()))
                .tags(c.getTags() == null ? List.of() : c.getTags().stream().map(this::toResponse).collect(Collectors.toList()))
                .build();
    }

    private org.acssz.ezclaim.domain.PayoutInfo toDomain(ClaimRequest.PayoutInfo p) {
        if (p == null) return null;
        return org.acssz.ezclaim.domain.PayoutInfo.builder()
                .bankName(p.getBankName())
                .accountNumber(p.getAccountNumber())
                .iban(p.getIban())
                .swift(p.getSwift())
                .routingNumber(p.getRoutingNumber())
                .bankAddress(p.getBankAddress())
                .build();
    }

    private PhotoResponse toResponse(Photo p) {
        return PhotoResponse.builder()
                .id(p.getId())
                .bucket(p.getBucket())
                .key(p.getKey())
                .uploadedAt(p.getUploadedAt())
                .build();
    }

    private TagResponse toResponse(Tag t) {
        return TagResponse.builder()
                .id(t.getId())
                .label(t.getLabel())
                .color(t.getColor())
                .build();
    }
}
