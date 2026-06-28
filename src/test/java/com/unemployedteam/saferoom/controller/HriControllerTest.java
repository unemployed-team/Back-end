package com.unemployedteam.saferoom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import com.unemployedteam.saferoom.global.exception.GlobalExceptionHandler;
import com.unemployedteam.saferoom.hri.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HriController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class HriControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private HriService hriService;

  @MockBean
  private RiskPredictionService riskPredictionService;

  @MockBean
  private CodefAuctionService codefAuctionService;

  @MockBean
  private AuctionHistoryRepository auctionHistoryRepository;

  @MockBean
  private com.unemployedteam.saferoom.building.BuildingRepository buildingRepository;

  private HriReportResponse sampleReport() {
    return HriReportResponse.builder()
        .reportId(1L)
        .buildingId(10L)
        .roadAddress("대구광역시 북구 대학로 80")
        .totalScore(45)
        .riskGrade("CAUTION")
        .buildingRiskScore(10)
        .marketRiskScore(15)
        .landlordRiskScore(15)
        .livingRiskScore(5)
        .recentTrades(List.of())
        .auctions(List.of())
        .shareUrl("/v1/hri/10/report")
        .calculatedAt(LocalDateTime.now())
        .riskProbability(0.4)
        .riskTrend("STABLE")
        .build();
  }

  @Test
  @DisplayName("HRI 리포트 조회 성공")
  void successGetReport() throws Exception {
    given(hriService.getOrCalculateReport(10L)).willReturn(sampleReport());

    mockMvc.perform(get("/hri/10/report"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalScore").value(45))
        .andExpect(jsonPath("$.riskGrade").value("CAUTION"))
        .andDo(print());
  }

  @Test
  @DisplayName("HRI 리포트 조회 실패 - 존재하지 않는 건물")
  void failGetReportNotFound() throws Exception {
    given(hriService.getOrCalculateReport(999L))
        .willThrow(new CustomException(ErrorCode.NOT_FOUND_BUILDING));

    mockMvc.perform(get("/hri/999/report"))
        .andExpect(status().isNotFound())
        .andDo(print());
  }

  @Test
  @DisplayName("HRI 재산출 성공")
  void successRecalculate() throws Exception {
    HriReportResponse recalculated = sampleReport();
    given(hriService.recalculate(10L)).willReturn(recalculated);

    mockMvc.perform(post("/hri/10/recalculate").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.buildingId").value(10))
        .andDo(print());
  }

  @Test
  @DisplayName("HRI 재산출 실패 - 존재하지 않는 건물")
  void failRecalculateNotFound() throws Exception {
    given(hriService.recalculate(999L))
        .willThrow(new CustomException(ErrorCode.NOT_FOUND_BUILDING));

    mockMvc.perform(post("/hri/999/recalculate").with(csrf()))
        .andExpect(status().isNotFound())
        .andDo(print());
  }

  @Test
  @DisplayName("보증금 위험 예측 조회 성공")
  void successPredict() throws Exception {
    RiskPredictionResult result = RiskPredictionResult.builder()
        .buildingId(10L)
        .riskProbability(0.35)
        .riskLevel("주의")
        .trend("STABLE")
        .currentJeonseRatio(0.65)
        .predictedJeonseRatio(0.67)
        .build();
    given(riskPredictionService.predict(10L)).willReturn(result);

    mockMvc.perform(get("/hri/10/prediction"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.riskProbability").value(0.35))
        .andExpect(jsonPath("$.trend").value("STABLE"))
        .andDo(print());
  }

  @Test
  @DisplayName("보증금 위험 예측 조회 실패 - 존재하지 않는 건물")
  void failPredictNotFound() throws Exception {
    given(riskPredictionService.predict(999L))
        .willThrow(new CustomException(ErrorCode.NOT_FOUND_BUILDING));

    mockMvc.perform(get("/hri/999/prediction"))
        .andExpect(status().isNotFound())
        .andDo(print());
  }

  @Test
  @DisplayName("경매 조회 및 저장 성공")
  void successSearchAndSaveAuction() throws Exception {
    AuctionSearchResult result = AuctionSearchResult.builder()
        .caseNumber("2025타경12345")
        .caseName("부동산임의경매")
        .finalResult("미종국")
        .status("PROCEEDING")
        .decisionDate("20250601")
        .claimAmount("50000000")
        .address("대구광역시 북구 대학로 80")
        .build();

    com.unemployedteam.saferoom.building.Building mockBuilding =
        com.unemployedteam.saferoom.building.Building.builder()
            .pnuCode("2711010100100010000")
            .roadAddress("대구광역시 북구 대학로 80")
            .latitude(35.88).longitude(128.60)
            .build();

    given(codefAuctionService.searchAuction(anyString(), anyString(), anyString()))
        .willReturn(result);
    given(buildingRepository.findById(10L))
        .willReturn(java.util.Optional.of(mockBuilding));
    given(auctionHistoryRepository.save(any())).willReturn(null);
    given(hriService.recalculate(10L)).willReturn(sampleReport());

    AuctionSearchRequest req = new AuctionSearchRequest("대구지방법원", "2025", "12345");

    mockMvc.perform(post("/hri/10/auction/search")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PROCEEDING"))
        .andDo(print());
  }

  @Test
  @DisplayName("경매 조회 실패 - 존재하지 않는 건물")
  void failSearchAuctionBuildingNotFound() throws Exception {
    given(codefAuctionService.searchAuction(anyString(), anyString(), anyString()))
        .willReturn(AuctionSearchResult.builder().status("PROCEEDING").build());
    given(buildingRepository.findById(999L)).willReturn(java.util.Optional.empty());

    AuctionSearchRequest req = new AuctionSearchRequest("대구지방법원", "2025", "12345");

    mockMvc.perform(post("/hri/999/auction/search")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isNotFound())
        .andDo(print());
  }
}