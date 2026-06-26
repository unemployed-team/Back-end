package com.unemployedteam.saferoom.hri;

import com.unemployedteam.saferoom.building.Building;
import com.unemployedteam.saferoom.building.BuildingDetail;
import com.unemployedteam.saferoom.fieldreport.FieldReport;
import com.unemployedteam.saferoom.fieldreport.FieldReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * HRI Score 산출 알고리즘
 * <p>
 * 4대 카테고리 가중치: - 건축 위험: 25점 - 시세 이상: 25점 - 임대인 위험: 30점 - 생활 안전: 20점 합계: 100점 (높을수록 위험)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HriCalculator {

  private final TradePriceRepository tradePriceRepository;
  private final AuctionHistoryRepository auctionHistoryRepository;
  private final FieldReportRepository fieldReportRepository;

  public HriScore calculate(Building building) {
    int buildingRisk = calcBuildingRisk(building);
    int marketRisk = calcMarketRisk(building);
    int landlordRisk = calcLandlordRisk(building);
    int livingRisk = calcLivingRisk(building);

    int fieldReportPenalty = calcFieldReportPenalty(building.getId());

    int total = Math.min(100,
        buildingRisk + marketRisk + landlordRisk + livingRisk + fieldReportPenalty);

    return HriScore.builder()
        .building(building)
        .buildingRiskScore(buildingRisk)
        .marketRiskScore(marketRisk)
        .landlordRiskScore(landlordRisk)
        .livingRiskScore(livingRisk)
        .totalScore(total)
        .riskGrade(HriScore.calcGrade(total))
        .build();
  }

  private int calcBuildingRisk(Building building) {
    int score = 0;
    BuildingDetail detail = building.getDetail();

    if (detail == null) {
      return 15;
    }

    if (Boolean.TRUE.equals(detail.getIsIllegalBuilding())) {
      score += 20;
    }

    if (building.getBuildYear() != null) {
      int age = LocalDate.now().getYear() - building.getBuildYear();
      if (age >= 30) {
        score += 5;
      } else if (age >= 20) {
        score += 3;
      } else if (age >= 15) {
        score += 1;
      }
    }

    if (detail.getHouseholdCount() != null && detail.getFloorCount() != null
        && detail.getFloorCount() > 0) {
      double ratio = (double) detail.getHouseholdCount() / detail.getFloorCount();
      if (ratio > 10) {
        score += 5;
      }
    }

    return Math.min(25, score);
  }

  private int calcMarketRisk(Building building) {
    int score = 0;
    String sixMonthsAgo = LocalDate.now().minusMonths(6)
        .format(DateTimeFormatter.ofPattern("yyyyMM"));

    Double avgDeposit = tradePriceRepository.findAvgPrice(
        building.getId(), "RENT_DEPOSIT", sixMonthsAgo);
    Double avgSale = tradePriceRepository.findAvgPrice(
        building.getId(), "SALE", sixMonthsAgo);

    if (avgDeposit == null || avgSale == null || avgSale == 0) {
      return 10;
    }

    double jeonseRatio = avgDeposit / avgSale;

    if (jeonseRatio >= 0.9) {
      score += 25;
    } else if (jeonseRatio >= 0.8) {
      score += 20;
    } else if (jeonseRatio >= 0.7) {
      score += 15;
    } else if (jeonseRatio >= 0.6) {
      score += 8;
    } else {
      score += 0;
    }

    return Math.min(25, score);
  }

  private int calcLandlordRisk(Building building) {
    int score = 0;

    boolean hasActiveAuction = auctionHistoryRepository
        .existsByBuildingIdAndAuctionStatus(building.getId(), "PROCEEDING");
    if (hasActiveAuction) {
      score += 30;
    }

    List<AuctionHistory> histories = auctionHistoryRepository
        .findByBuildingIdOrderByCreatedAtDesc(building.getId());
    if (!hasActiveAuction && !histories.isEmpty()) {
      score += 10;
    }

    return Math.min(30, score);
  }

  private int calcLivingRisk(Building building) {
    int score = 5;
    return Math.min(20, score);
  }

  private int calcFieldReportPenalty(Long buildingId) {
    List<FieldReport> reports = fieldReportRepository
        .findVerifiedByBuildingId(buildingId);
    return reports.stream()
        .mapToInt(FieldReport::getImpactScore)
        .sum();
  }
}