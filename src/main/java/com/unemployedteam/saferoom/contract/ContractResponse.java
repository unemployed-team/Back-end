package com.unemployedteam.saferoom.contract;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Builder
public class ContractResponse {

  private Long contractId;
  private Long buildingId;
  private String roadAddress;
  private Long deposit;
  private Long monthlyRent;
  private LocalDate contractStart;
  private LocalDate contractEnd;
  private Long daysUntilExpiry;
  private String expiryAlert;
  private LocalDateTime createdAt;

  public static ContractResponse from(Contract c) {
    long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), c.getContractEnd());
    String alert = null;
    if (daysLeft <= 7) {
      alert = "D-7";
    } else if (daysLeft <= 30) {
      alert = "D-30";
    } else if (daysLeft <= 90) {
      alert = "D-90";
    }

    return ContractResponse.builder()
        .contractId(c.getId())
        .buildingId(c.getBuilding().getId())
        .roadAddress(c.getBuilding().getRoadAddress())
        .deposit(c.getDeposit())
        .monthlyRent(c.getMonthlyRent())
        .contractStart(c.getContractStart())
        .contractEnd(c.getContractEnd())
        .daysUntilExpiry(daysLeft)
        .expiryAlert(alert)
        .createdAt(c.getCreatedAt())
        .build();
  }
}
