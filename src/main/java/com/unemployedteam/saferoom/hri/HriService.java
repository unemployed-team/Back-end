package com.unemployedteam.saferoom.hri;

import com.unemployedteam.saferoom.building.Building;
import com.unemployedteam.saferoom.building.BuildingRepository;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HriService {

  private final BuildingRepository buildingRepository;
  private final HriScoreRepository hriScoreRepository;
  private final TradePriceRepository tradePriceRepository;
  private final AuctionHistoryRepository auctionHistoryRepository;
  private final HriCalculator hriCalculator;
  private final RiskPredictionService riskPredictionService;

  @Transactional
  public HriReportResponse getOrCalculateReport(Long buildingId) {
    Building building = buildingRepository.findById(buildingId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_BUILDING));
    HriScore score = hriScoreRepository.findLatestByBuildingId(buildingId)
        .orElseGet(() -> hriScoreRepository.save(hriCalculator.calculate(building)));
    return buildResponse(building, score);
  }

  @Transactional
  public HriReportResponse recalculate(Long buildingId) {
    Building building = buildingRepository.findById(buildingId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_BUILDING));
    HriScore score = hriScoreRepository.save(hriCalculator.calculate(building));
    return buildResponse(building, score);
  }

  private HriReportResponse buildResponse(Building building, HriScore score) {
    List<TradePrice> trades = tradePriceRepository.findRecent12Months(building.getId());
    List<AuctionHistory> auctions = auctionHistoryRepository
        .findByBuildingIdOrderByCreatedAtDesc(building.getId());

    String sixMonthsAgo = LocalDate.now().minusMonths(6)
        .format(DateTimeFormatter.ofPattern("yyyyMM"));

    Double bDeposit = tradePriceRepository.findAvgPrice(
        building.getId(), "RENT_DEPOSIT", sixMonthsAgo);
    Double bSale = tradePriceRepository.findAvgPrice(
        building.getId(), "SALE", sixMonthsAgo);
    Double buildingRatio = (bDeposit != null && bSale != null && bSale > 0)
        ? bDeposit / bSale : null;

    String district = extractDistrict(building.getRoadAddress());
    Double dDeposit = tradePriceRepository.findDistrictAvgPrice(
        "%" + district + "%", "RENT_DEPOSIT", sixMonthsAgo);
    Double dSale = tradePriceRepository.findDistrictAvgPrice(
        "%" + district + "%", "SALE", sixMonthsAgo);
    Double districtRatio = (dDeposit != null && dSale != null && dSale > 0)
        ? dDeposit / dSale : null;

    RiskPredictionResult prediction = riskPredictionService.predict(building.getId());

    return HriReportResponse.builder()
        .reportId(score.getId())
        .buildingId(building.getId())
        .roadAddress(building.getRoadAddress())
        .totalScore(score.getTotalScore())
        .riskGrade(score.getRiskGrade())
        .buildingRiskScore(score.getBuildingRiskScore())
        .marketRiskScore(score.getMarketRiskScore())
        .landlordRiskScore(score.getLandlordRiskScore())
        .livingRiskScore(score.getLivingRiskScore())
        .recentTrades(trades.stream().map(HriReportResponse.TradePriceDto::from).toList())
        .auctions(auctions.stream().map(HriReportResponse.AuctionDto::from).toList())
        .shareUrl("/v1/hri/" + building.getId() + "/report")
        .calculatedAt(score.getCreatedAt())
        .riskProbability(prediction.getRiskProbability())
        .riskTrend(prediction.getTrend())
        .predictedJeonseRatio(prediction.getPredictedJeonseRatio())
        .buildingJeonseRatio(buildingRatio)
        .districtAvgJeonseRatio(districtRatio)
        .districtCompareComment(buildCompareComment(buildingRatio, districtRatio))
        .build();
  }

  private String extractDistrict(String roadAddress) {
    if (roadAddress == null || roadAddress.isBlank()) {
      return "";
    }
    String[] parts = roadAddress.split(" ");
    return parts.length >= 2 ? parts[1] : "";
  }

  private String buildCompareComment(Double building, Double district) {
    if (building == null || district == null) {
      return null;
    }
    double diff = (building - district) * 100;
    if (diff > 5) {
      return String.format("이 건물은 주변 평균보다 전세가율이 %.0f%% 높습니다 (주의)", diff);
    }
    if (diff < -5) {
      return String.format("이 건물은 주변 평균보다 전세가율이 %.0f%% 낮습니다 (안전)", Math.abs(diff));
    }
    return "이 건물의 전세가율은 주변 평균 수준입니다";
  }
}