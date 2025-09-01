package org.acssz.ezclaim.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

import org.acssz.ezclaim.domain.ClaimStatus;
import org.acssz.ezclaim.domain.Currency;

import lombok.Data;

@Data
public class ClaimPatchRequest {
    private String title;           // optional (admin only)
    private String description;     // optional (admin only)
    private ClaimStatus status;     // optional with transition rules
    private BigDecimal amount;      // optional (admin only)
    private Currency currency;      // optional (admin only)
    private ClaimRequest.PayoutInfo payout; // optional (admin only)
    private String recipient;       // optional (admin only)
    private Instant expenseAt;      // optional (admin only)

    // For anonymous updates when claim is password-protected
    private String password;
}

