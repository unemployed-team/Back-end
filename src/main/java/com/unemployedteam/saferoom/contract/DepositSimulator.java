package com.unemployedteam.saferoom.contract;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 경매 배당 시뮬레이터
 * <p>
 * 알고리즘: 1. 경매 낙찰가 = 공시가격 × 지역별 낙찰가율 2. 소액임차인 최우선변제금 먼저 배당 (주택임대차보호법) 3. 선순위 근저당 배당 4. 잔액에서 내 보증금
 * 회수율 산출
 */
@Slf4j
@Component
public class DepositSimulator {

  // 대구 지역 평균 낙찰가율 (대법원 통계 기준 약 72%)
  private static final double DAEGU_AUCTION_RATE = 0.72;

  // 소액임차인 최우선변제금 (대구 기준: 보증금 5500만 이하 → 최우선 1650만 보호)
  private static final long SMALL_TENANT_MAX_DEPOSIT = 55_000_000L;
  private static final long SMALL_TENANT_PROTECTION = 16_500_000L;

  public SimulationResult simulate(long deposit, long officialPrice, long priorMortgage) {
    long auctionPrice = (long) (officialPrice * DAEGU_AUCTION_RATE);

    long smallTenantProtection = deposit <= SMALL_TENANT_MAX_DEPOSIT
        ? Math.min(SMALL_TENANT_PROTECTION, deposit)
        : 0L;

    long afterSmallTenant = Math.max(0, auctionPrice - smallTenantProtection);

    long afterMortgage = Math.max(0, afterSmallTenant - priorMortgage);

    long additionalRecovery = Math.min(
        Math.max(0, deposit - smallTenantProtection),
        afterMortgage
    );
    long totalRecovery = smallTenantProtection + additionalRecovery;

    long expectedLoss = Math.max(0, deposit - totalRecovery);
    double recoveryRate = deposit > 0 ? (double) totalRecovery / deposit : 0.0;

    return SimulationResult.builder()
        .auctionPrice(auctionPrice)
        .smallTenantProtection(smallTenantProtection)
        .remainingAfterMortgage(afterMortgage)
        .expectedRecovery(totalRecovery)
        .expectedLoss(expectedLoss)
        .recoveryRate(recoveryRate)
        .build();
  }

  @lombok.Builder
  @lombok.Getter
  public static class SimulationResult {

    private long auctionPrice;
    private long smallTenantProtection;
    private long remainingAfterMortgage;
    private long expectedRecovery;
    private long expectedLoss;
    private double recoveryRate;
  }
}