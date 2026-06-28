package com.unemployedteam.saferoom.contract;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SimulationResponse {

  private Long contractId;
  private Long deposit;
  private Long officialPrice;
  private Long priorMortgage;
  private Long auctionPrice;
  private Long smallTenantProtection;
  private Long remainingAfterMortgage;
  private Long expectedRecovery;
  private Long expectedLoss;
  private Double recoveryRate;
  private String riskComment;
  private LocalDateTime calculatedAt;

  public static SimulationResponse from(DepositSimulation sim, Long deposit,
      Long officialPrice, Long priorMortgage,
      Long smallTenantProtection) {
    return SimulationResponse.builder()
        .contractId(sim.getContract().getId())
        .deposit(deposit)
        .officialPrice(officialPrice)
        .priorMortgage(priorMortgage)
        .auctionPrice(sim.getAuctionPrice())
        .smallTenantProtection(smallTenantProtection)
        .expectedLoss(sim.getExpectedLoss())
        .expectedRecovery(deposit - sim.getExpectedLoss())
        .recoveryRate(sim.getRecoveryRate())
        .riskComment(buildComment(sim.getRecoveryRate()))
        .calculatedAt(sim.getCalculatedAt())
        .build();
  }

  private static String buildComment(double rate) {
    if (rate >= 0.9) {
      return "보증금 대부분 회수 가능 (안전)";
    }
    if (rate >= 0.7) {
      return "보증금 일부 손실 가능성 있음 (주의)";
    }
    if (rate >= 0.5) {
      return "보증금 상당 손실 예상 (위험)";
    }
    return "보증금 회수 매우 어려움 (매우 위험)";
  }
}