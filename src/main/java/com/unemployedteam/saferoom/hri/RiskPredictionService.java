package com.unemployedteam.saferoom.hri;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiskPredictionService {

  private final TradePriceRepository tradePriceRepository;

  public RiskPredictionResult predict(Long buildingId) {
    List<TradePrice> trades = tradePriceRepository.findRecent12Months(buildingId);

    if (trades.size() < 3) {
      return RiskPredictionResult.builder()
          .buildingId(buildingId)
          .riskProbability(0.3)
          .riskLevel("데이터 부족 — 주의")
          .trend("UNKNOWN")
          .build();
    }

    String sixMonthsAgo = LocalDate.now().minusMonths(6)
        .format(DateTimeFormatter.ofPattern("yyyyMM"));

    double recentDeposit = trades.stream()
        .filter(t -> "RENT_DEPOSIT".equals(t.getTradeType()))
        .filter(t -> t.getContractYearMonth().compareTo(sixMonthsAgo) >= 0)
        .mapToLong(TradePrice::getPrice).average().orElse(0);

    double oldDeposit = trades.stream()
        .filter(t -> "RENT_DEPOSIT".equals(t.getTradeType()))
        .filter(t -> t.getContractYearMonth().compareTo(sixMonthsAgo) < 0)
        .mapToLong(TradePrice::getPrice).average().orElse(0);

    double recentSale = trades.stream()
        .filter(t -> "SALE".equals(t.getTradeType()))
        .filter(t -> t.getContractYearMonth().compareTo(sixMonthsAgo) >= 0)
        .mapToLong(TradePrice::getPrice).average().orElse(0);

    double currentRatio = recentSale > 0 ? recentDeposit / recentSale : 0.5;
    double depositTrend = oldDeposit > 0 ? (recentDeposit - oldDeposit) / oldDeposit : 0;
    double predictedRatio = currentRatio + (depositTrend * 2);
    double riskProbability = calcRisk(currentRatio, predictedRatio, depositTrend);

    String trend = depositTrend > 0.05 ? "RISING"
        : depositTrend < -0.05 ? "FALLING" : "STABLE";

    String riskLevel = riskProbability >= 0.7 ? "매우 위험"
        : riskProbability >= 0.5 ? "위험"
            : riskProbability >= 0.3 ? "주의" : "안전";

    return RiskPredictionResult.builder()
        .buildingId(buildingId)
        .riskProbability(Math.min(1.0, riskProbability))
        .riskLevel(riskLevel)
        .trend(trend)
        .currentJeonseRatio(currentRatio)
        .predictedJeonseRatio(Math.min(1.5, predictedRatio))
        .build();
  }

  private double calcRisk(double current, double predicted, double trend) {
    double base = current >= 0.9 ? 0.8
        : current >= 0.8 ? 0.6
            : current >= 0.7 ? 0.4
                : current >= 0.6 ? 0.2 : 0.05;
    return base + Math.max(0, trend) * 0.5 + Math.max(0, predicted - 1.0) * 0.3;
  }
}