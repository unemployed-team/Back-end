package com.unemployedteam.saferoom.hri;

import com.unemployedteam.saferoom.building.Building;
import com.unemployedteam.saferoom.building.BuildingRepository;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HriService {

  private final BuildingRepository buildingRepository;
  private final HriScoreRepository hriScoreRepository;
  private final TradePriceRepository tradePriceRepository;
  private final AuctionHistoryRepository auctionHistoryRepository;
  private final HriCalculator hriCalculator;

  @Transactional
  public HriReportResponse getOrCalculateReport(Long buildingId) {
    Building building = buildingRepository.findById(buildingId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_BUILDING));

    HriScore score = hriScoreRepository.findLatestByBuildingId(buildingId)
        .orElseGet(() -> {
          HriScore calc = hriCalculator.calculate(building);
          return hriScoreRepository.save(calc);
        });

    return buildResponse(building, score);
  }

  @Transactional
  public HriReportResponse recalculate(Long buildingId) {
    Building building = buildingRepository.findById(buildingId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_BUILDING));

    HriScore score = hriCalculator.calculate(building);
    hriScoreRepository.save(score);
    return buildResponse(building, score);
  }

  private HriReportResponse buildResponse(Building building, HriScore score) {
    List<TradePrice> trades = tradePriceRepository.findRecent12Months(building.getId());
    List<AuctionHistory> auctions = auctionHistoryRepository
        .findByBuildingIdOrderByCreatedAtDesc(building.getId());

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
        .build();
  }
}