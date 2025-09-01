package org.acssz.ezclaim.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

import org.acssz.ezclaim.domain.ClaimStatus;
import org.acssz.ezclaim.domain.Currency;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(name = "ClaimPatchRequest", description = "Fields to update on a claim")
public class ClaimPatchRequest {
    @Schema(description = "Title (admin only)")
    private String title;           // optional (admin only)
    @Schema(description = "Description (admin only)")
    private String description;     // optional (admin only)
    @Schema(description = "New status with transition rules")
    private ClaimStatus status;     // optional with transition rules
    @Schema(description = "Amount (admin only)")
    private BigDecimal amount;      // optional (admin only)
    @Schema(description = "Currency (admin only)")
    private Currency currency;      // optional (admin only)
    @Schema(description = "Payout info (admin only)")
    private ClaimRequest.PayoutInfo payout; // optional (admin only)
    @Schema(description = "Recipient (admin only)")
    private String recipient;       // optional (admin only)
    @Schema(description = "Expense time (admin only)")
    private Instant expenseAt;      // optional (admin only)

    // For anonymous updates when claim is password-protected
    @Schema(description = "Password for anonymous updates")
    private String password;
}
