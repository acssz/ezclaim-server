package org.acssz.ezclaim.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.acssz.ezclaim.domain.ClaimStatus;
import org.acssz.ezclaim.domain.Currency;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ClaimResponse {
    String id;
    String title;
    String description;
    ClaimStatus status;
    Instant createdAt;
    Instant updatedAt;

    BigDecimal amount;
    Currency currency;
    String recipient;
    Instant expenseAt;
    PayoutInfo payout;

    List<PhotoResponse> photos;
    List<TagResponse> tags;

    @Value
    @Builder
    public static class PayoutInfo {
        String bankName;
        String accountNumber;
        String iban;
        String swift;
        String routingNumber;
        String bankAddress;
    }
}
