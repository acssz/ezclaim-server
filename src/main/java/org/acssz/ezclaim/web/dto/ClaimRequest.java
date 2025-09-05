package org.acssz.ezclaim.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import org.acssz.ezclaim.domain.ClaimStatus;
import org.acssz.ezclaim.domain.Currency;

@Data
@Schema(name = "ClaimRequest", description = "Payload to create a claim")
public class ClaimRequest {
  @NotBlank @Schema(description = "Short title of the claim", example = "Conference Ticket")
  private String title;

  @Schema(description = "Optional longer description", example = "Ticket for QCon 2025")
  private String description; // optional

  @Schema(description = "Initial status (optional)")
  private ClaimStatus status; // optional; defaults to SUBMITTED if null

  // Optional references by id, if you want to attach existing photos/tags
  @Schema(description = "IDs of uploaded photos to attach")
  private List<String> photoIds;

  @Schema(description = "IDs of tags to attach")
  private List<String> tagIds;

  @NotNull @Positive @Schema(description = "Amount to reimburse", example = "199.99")
  private BigDecimal amount;

  @Schema(description = "Currency (defaults to CHF)", example = "CHF")
  private Currency currency; // defaults to CHF if null

  @NotNull @Schema(description = "Payout destination info")
  private PayoutInfo payout; // optional structured payout info

  @Schema(description = "Recipient display name", example = "Alice")
  private String recipient; // optional display name

  @NotNull @Schema(description = "When the expense occurred (ISO-8601)", example = "2025-08-12T09:30:00Z")
  private Instant expenseAt; // when the expense occurred

  // Optional password for anonymous access control (write-only)
  @Schema(description = "Optional password to protect claim", example = "secret123")
  private String password;

  @Data
  public static class PayoutInfo {
    @Schema(example = "ACME Bank")
    private String bankName;

    @Schema(example = "123456789")
    private String accountNumber;

    @Schema(example = "CH93 0076 2011 6238 5295 7")
    private String iban;

    @Schema(example = "POFICHBEXXX")
    private String swift;

    @Schema(example = "021000021")
    private String routingNumber;

    @Schema(example = "Some Street 1, 8000 Zurich")
    private String bankAddress;
  }
}
