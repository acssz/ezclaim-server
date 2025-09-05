package org.acssz.ezclaim.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import org.acssz.ezclaim.domain.ClaimStatus;
import org.acssz.ezclaim.domain.Currency;

@Value
@Builder
@Schema(name = "ClaimResponse", description = "Claim representation")
public class ClaimResponse {
  @Schema(example = "664a0c2f7b1f3c2d9b7c9a10")
  String id;

  @Schema(example = "Conference Ticket")
  String title;

  @Schema(example = "Ticket for QCon 2025")
  String description;

  @Schema(example = "SUBMITTED")
  ClaimStatus status;

  @Schema(example = "2025-08-12T09:31:00Z")
  Instant createdAt;

  @Schema(example = "2025-08-12T09:31:00Z")
  Instant updatedAt;

  @Schema(example = "199.99")
  BigDecimal amount;

  @Schema(example = "CHF")
  Currency currency;

  @Schema(example = "Alice")
  String recipient;

  @Schema(example = "2025-08-10T14:00:00Z")
  Instant expenseAt;

  PayoutInfo payout;

  List<PhotoResponse> photos;
  List<TagResponse> tags;

  @Value
  @Builder
  @Schema(name = "PayoutInfo")
  public static class PayoutInfo {
    @Schema(example = "ACME Bank")
    String bankName;

    @Schema(example = "123456789")
    String accountNumber;

    @Schema(example = "CH93 0076 2011 6238 5295 7")
    String iban;

    @Schema(example = "POFICHBEXXX")
    String swift;

    @Schema(example = "021000021")
    String routingNumber;

    @Schema(example = "Some Street 1, 8000 Zurich")
    String bankAddress;
  }
}
