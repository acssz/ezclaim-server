package org.acssz.ezclaim.domain;

public enum ClaimStatus {
  UNKNOWN, // reserved for errors
  SUBMITTED, // initial state after creation
  APPROVED, // admin: SUBMITTED -> APPROVED
  PAID, // admin: APPROVED -> PAID
  FINISHED, // accessible user: PAID -> FINISHED
  REJECTED, // admin: from any (except FINISHED, WITHDRAW) -> REJECTED
  PAYMENT_FAILED, // admin: from APPROVED -> PAYMENT_FAILED
  WITHDRAW // accessible user: SUBMITTED -> WITHDRAW
}
