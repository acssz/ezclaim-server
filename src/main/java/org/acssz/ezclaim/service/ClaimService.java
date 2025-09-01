package org.acssz.ezclaim.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.acssz.ezclaim.domain.Claim;
import org.acssz.ezclaim.domain.ClaimStatus;
import org.acssz.ezclaim.domain.Currency;
import org.acssz.ezclaim.domain.PayoutInfo;
import org.acssz.ezclaim.domain.Photo;
import org.acssz.ezclaim.domain.Tag;
import org.acssz.ezclaim.repository.ClaimRepository;
import org.acssz.ezclaim.repository.PhotoRepository;
import org.acssz.ezclaim.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimService {
    private final ClaimRepository repository;
    private final PhotoRepository photoRepository;
    private final TagRepository tagRepository;

    public List<Claim> findAll() {
        return repository.findAll();
    }

    public Claim findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + id));
    }

    public Claim create(String title, String description, ClaimStatus status, List<String> photoIds,
            List<String> tagIds, BigDecimal amount, Currency currency, PayoutInfo payout, String recipient,
            String passwordRaw, PasswordEncoder encoder, Instant expenseAt) {
        Instant now = Instant.now();
        Claim claim = Claim.builder()
                .title(title)
                .description(description)
                .status(status != null ? status : ClaimStatus.SUBMITTED)
                .createdAt(now)
                .updatedAt(now)
                .photos(resolvePhotos(photoIds))
                .tags(resolveTags(tagIds))
                .amount(amount)
                .currency(currency != null ? currency : org.acssz.ezclaim.domain.Currency.CHF)
                .payout(payout)
                .recipient(recipient)
                .passwordHash(passwordRaw != null
                        && !passwordRaw.isBlank()
                        && encoder != null ? encoder.encode(passwordRaw) : null)
                .expenseAt(expenseAt)
                .build();
        Claim saved = repository.save(claim);
        log.info("Created claim {}", saved.getId());
        return saved;
    }

    public Claim update(String id, String title, String description, ClaimStatus status,
            List<String> photoIds, List<String> tagIds) {
        Claim existing = findById(id);
        existing.setTitle(title);
        existing.setDescription(description);
        if (status != null)
            existing.setStatus(status);
        if (photoIds != null)
            existing.setPhotos(resolvePhotos(photoIds));
        if (tagIds != null)
            existing.setTags(resolveTags(tagIds));
        // optional new fields updates
        // null means no change
        // amount, currency, payout, recipient, expenseAt
        // password change intentionally not supported here unless explicitly provided
        // (for admin flows you may add a dedicated endpoint)
        // keep minimal per request
        existing.setUpdatedAt(Instant.now());
        return repository.save(existing);
    }

    public void delete(String id) {
        Claim existing = findById(id);
        repository.delete(existing);
        log.info("Deleted claim {}", id);
    }

    public record Patch(ClaimStatus status,
            BigDecimal amount,
            Currency currency,
            PayoutInfo payout,
            String recipient,
            String title,
            String description,
            Instant expenseAt) {
    }

    public Claim patch(String id, Patch patch, boolean privileged, boolean allowUserStatusChange) {
        Claim c = findById(id);

        // Handle status transitions
        if (patch.status() != null) {
            ClaimStatus from = c.getStatus();
            ClaimStatus to = patch.status();
            boolean ok = false;
            if (privileged) {
                ok = isAdminTransitionAllowed(from, to);
            } else if (allowUserStatusChange) {
                ok = isUserTransitionAllowed(from, to);
            }
            if (!ok)
                throw new AccessDeniedException("status transition not allowed");
            c.setStatus(to);
        }

        // Other fields only editable by privileged
        if (!privileged && (patch.amount() != null || patch.currency() != null || patch.payout() != null
                || patch.recipient() != null || patch.title() != null || patch.description() != null
                || patch.expenseAt() != null)) {
            throw new AccessDeniedException("field update not allowed");
        }

        if (privileged) {
            if (patch.title() != null)
                c.setTitle(patch.title());
            if (patch.description() != null)
                c.setDescription(patch.description());
            if (patch.amount() != null)
                c.setAmount(patch.amount());
            if (patch.currency() != null)
                c.setCurrency(patch.currency());
            if (patch.payout() != null)
                c.setPayout(patch.payout());
            if (patch.recipient() != null)
                c.setRecipient(patch.recipient());
            if (patch.expenseAt() != null)
                c.setExpenseAt(patch.expenseAt());
        }

        c.setUpdatedAt(Instant.now());
        return repository.save(c);
    }

    private boolean isAdminTransitionAllowed(ClaimStatus from, ClaimStatus to) {
        if (to == ClaimStatus.REJECTED) {
            return from != ClaimStatus.FINISHED && from != ClaimStatus.WITHDRAW;
        }
        return (from == ClaimStatus.SUBMITTED && to == ClaimStatus.APPROVED)
                || (from == ClaimStatus.APPROVED && to == ClaimStatus.PAID);
    }

    private boolean isUserTransitionAllowed(ClaimStatus from, ClaimStatus to) {
        return (from == ClaimStatus.SUBMITTED && to == ClaimStatus.WITHDRAW)
                || (from == ClaimStatus.PAID && to == ClaimStatus.FINISHED);
    }

    private List<Photo> resolvePhotos(List<String> ids) {
        if (ids == null || ids.isEmpty())
            return List.of();
        List<Photo> found = photoRepository.findAllById(ids);
        ensureAllFound(ids, found.stream().map(Photo::getId).filter(Objects::nonNull).toList(), "photo");
        return found;
    }

    private List<Tag> resolveTags(List<String> ids) {
        if (ids == null || ids.isEmpty())
            return List.of();
        List<Tag> found = tagRepository.findAllById(ids);
        ensureAllFound(ids, found.stream().map(Tag::getId).filter(Objects::nonNull).toList(), "tag");
        return found;
    }

    private void ensureAllFound(List<String> requested, List<String> found, String type) {
        Set<String> missing = new HashSet<>(requested);
        missing.removeAll(new HashSet<>(found));
        if (!missing.isEmpty()) {
            throw new ResourceNotFoundException("Unknown " + type + " id(s): " + String.join(",", missing));
        }
    }
}
