package org.acssz.ezclaim.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutInfo {
    private String bankName;
    private String accountNumber;
    private String iban;
    private String swift;
    private String routingNumber;
    private String bankAddress;
}