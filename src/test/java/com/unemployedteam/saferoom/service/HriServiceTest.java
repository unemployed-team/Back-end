package com.unemployedteam.saferoom.service;

import com.unemployedteam.saferoom.building.Building;
import com.unemployedteam.saferoom.building.BuildingRepository;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import com.unemployedteam.saferoom.hri.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class HriServiceTest {

  @InjectMocks
  private HriService hriService;

  @Mock
  private BuildingRepository buildingRepository;
  @Mock
  private HriScoreRepository hriScoreRepository;
  @Mock
  private TradePriceRepository tradePriceRepository;
  @Mock
  private AuctionHistoryRepository auctionHistoryRepository;
  @Mock
  private HriCalculator hriCalculator;
  @Mock
  private RiskPredictionService riskPredictionService;

  private Building mockBuilding() {
    return Building.builder()
        .id(10L).pnuCode("2711010100100010000")
        .roadAddress("대구광역시 북구 대학로 80")
        .latitude(35.88).longitude(128.60)
        .build();
  }

  private HriScore mockScore(Building building) {
    return HriScore.builder()
        .id(1L).building(building)
        .totalScore(45).riskGrade("CAUTION")
        .buildingRiskScore(10).marketRiskScore(15)
        .landlordRiskScore(15).livingRiskScore(5)
        .build();
  }

  private RiskPredictionResult mockPrediction() {
    return RiskPredictionResult.builder()
        .buildingId(10L).riskProbability(0.35)
        .riskLevel("주의").trend("STABLE")
        .currentJeonseRatio(0.65).predictedJeonseRatio(0.67)
        .build();
  }

  @Test
  @DisplayName("HRI 리포트 조회 성공 - 기존 점수 반환")
  void successGetOrCalculateReportCached() {
    Building building = mockBuilding();
    HriScore score = mockScore(building);

    given(buildingRepository.findById(10L)).willReturn(Optional.of(building));
    given(hriScoreRepository.findLatestByBuildingId(10L)).willReturn(Optional.of(score));
    given(tradePriceRepository.findRecent12Months(10L)).willReturn(List.of());
    given(auctionHistoryRepository.findByBuildingIdOrderByCreatedAtDesc(10L))
        .willReturn(List.of());
    given(tradePriceRepository.findAvgPrice(anyLong(), anyString(), anyString()))
        .willReturn(null);
    given(tradePriceRepository.findDistrictAvgPrice(anyString(), anyString(), anyString()))
        .willReturn(null);
    given(riskPredictionService.predict(10L)).willReturn(mockPrediction());

    HriReportResponse result = hriService.getOrCalculateReport(10L);

    assertNotNull(result);
    assertEquals(45, result.getTotalScore());
    assertEquals("CAUTION", result.getRiskGrade());
    verify(hriCalculator, never()).calculate(any()); // 재계산 없음
  }

  @Test
  @DisplayName("HRI 리포트 조회 실패 - 건물 없음")
  void failGetOrCalculateReportBuildingNotFound() {
    given(buildingRepository.findById(999L)).willReturn(Optional.empty());

    CustomException ex = assertThrows(CustomException.class,
        () -> hriService.getOrCalculateReport(999L));
    assertEquals(ErrorCode.NOT_FOUND_BUILDING, ex.getErrorCode());
  }

  @Test
  @DisplayName("HRI 재산출 성공")
  void successRecalculate() {
    Building building = mockBuilding();
    HriScore score = mockScore(building);

    given(buildingRepository.findById(10L)).willReturn(Optional.of(building));
    given(hriCalculator.calculate(building)).willReturn(score);
    given(hriScoreRepository.save(score)).willReturn(score);
    given(tradePriceRepository.findRecent12Months(10L)).willReturn(List.of());
    given(auctionHistoryRepository.findByBuildingIdOrderByCreatedAtDesc(10L))
        .willReturn(List.of());
    given(tradePriceRepository.findAvgPrice(anyLong(), anyString(), anyString()))
        .willReturn(null);
    given(tradePriceRepository.findDistrictAvgPrice(anyString(), anyString(), anyString()))
        .willReturn(null);
    given(riskPredictionService.predict(10L)).willReturn(mockPrediction());

    HriReportResponse result = hriService.recalculate(10L);

    assertNotNull(result);
    verify(hriCalculator).calculate(building);
    verify(hriScoreRepository).save(score);
  }

  @Test
  @DisplayName("HRI 재산출 실패 - 건물 없음")
  void failRecalculateBuildingNotFound() {
    given(buildingRepository.findById(999L)).willReturn(Optional.empty());

    assertThrows(CustomException.class, () -> hriService.recalculate(999L));
  }
}
