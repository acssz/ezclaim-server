package org.acssz.ezclaim.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.acssz.ezclaim.domain.ClaimStatus;
import org.acssz.ezclaim.domain.Currency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ClaimRequest {
    @NotBlank
    private String title;

    private String description; // optional

    private ClaimStatus status; // optional; defaults to SUBMITTED if null

    // Optional references by id, if you want to attach existing photos/tags
    private List<String> photoIds;
    private List<String> tagIds;

    @NotNull
    @Positive
    private BigDecimal amount;

    private Currency currency; // defaults to CHF if null
    
    @NotNull
    private PayoutInfo payout; // optional structured payout info

    private String recipient;  // optional display name

    @NotNull
    private Instant expenseAt; // when the expense occurred

    // Optional password for anonymous access control (write-only)
    private String password;

    @Data
    public static class PayoutInfo {
        private String bankName;
        private String accountNumber;
        private String iban;
        private String swift;
        private String routingNumber;
        private String bankAddress;
    }
}
