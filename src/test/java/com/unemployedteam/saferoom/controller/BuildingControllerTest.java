package com.unemployedteam.saferoom.controller;

import com.unemployedteam.saferoom.building.BuildingController;
import com.unemployedteam.saferoom.building.BuildingService;
import com.unemployedteam.saferoom.building.dto.BuildingResponse;
import com.unemployedteam.saferoom.building.dto.ClusterResponse;
import com.unemployedteam.saferoom.building.dto.HeatmapResponse;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import com.unemployedteam.saferoom.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BuildingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class BuildingControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private BuildingService buildingService;

  private BuildingResponse sampleBuilding() {
    return BuildingResponse.builder()
        .buildingId(1L)
        .buildingName("경북대 앞 원룸")
        .roadAddress("대구광역시 북구 대학로 80")
        .latitude(35.8882)
        .longitude(128.6097)
        .totalScore(45)
        .riskGrade("CAUTION")
        .build();
  }

  @Test
  @DisplayName("건물 키워드 검색 성공")
  void successSearchByKeyword() throws Exception {
    given(buildingService.searchByKeyword("대학로")).willReturn(List.of(sampleBuilding()));

    mockMvc.perform(get("/buildings/search").param("keyword", "대학로"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].buildingName").value("경북대 앞 원룸"))
        .andDo(print());
  }

  @Test
  @DisplayName("건물 키워드 검색 실패 - keyword 파라미터 누락")
  void failSearchByKeywordMissingParam() throws Exception {
    mockMvc.perform(get("/buildings/search"))
        .andExpect(status().isBadRequest())
        .andDo(print());
  }

  @Test
  @DisplayName("주변 건물 조회 성공")
  void successSearchNearby() throws Exception {
    given(buildingService.searchNearby(anyDouble(), anyDouble(), anyDouble()))
        .willReturn(List.of(sampleBuilding()));

    mockMvc.perform(get("/buildings/nearby")
            .param("lat", "35.8882")
            .param("lng", "128.6097")
            .param("radius", "500"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].buildingId").value(1))
        .andDo(print());
  }

  @Test
  @DisplayName("주변 건물 조회 실패 - lat 파라미터 누락")
  void failSearchNearbyMissingParam() throws Exception {
    mockMvc.perform(get("/buildings/nearby")
            .param("lng", "128.6097"))
        .andExpect(status().isBadRequest())
        .andDo(print());
  }

  @Test
  @DisplayName("히트맵 데이터 조회 성공")
  void successGetHeatmap() throws Exception {
    HeatmapResponse response = HeatmapResponse.builder()
        .points(List.of(
            HeatmapResponse.HeatmapPoint.builder()
                .lat(35.88).lng(128.60).score(45).riskGrade("CAUTION").buildingId(1L)
                .build()
        )).build();
    given(buildingService.getHeatmap(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
        .willReturn(response);

    mockMvc.perform(get("/buildings/heatmap")
            .param("swLat", "35.8")
            .param("swLng", "128.5")
            .param("neLat", "35.95")
            .param("neLng", "128.7"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.points[0].riskGrade").value("CAUTION"))
        .andDo(print());
  }

  @Test
  @DisplayName("히트맵 데이터 조회 실패 - 필수 파라미터 누락")
  void failGetHeatmapMissingParam() throws Exception {
    mockMvc.perform(get("/buildings/heatmap")
            .param("swLat", "35.8"))
        .andExpect(status().isBadRequest())
        .andDo(print());
  }

  @Test
  @DisplayName("건물 상세 조회 성공")
  void successGetDetail() throws Exception {
    given(buildingService.getBuildingDetail(1L)).willReturn(sampleBuilding());

    mockMvc.perform(get("/buildings/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.riskGrade").value("CAUTION"))
        .andDo(print());
  }

  @Test
  @DisplayName("건물 상세 조회 실패 - 존재하지 않는 건물")
  void failGetDetailNotFound() throws Exception {
    given(buildingService.getBuildingDetail(999L))
        .willThrow(new CustomException(ErrorCode.NOT_FOUND_BUILDING));

    mockMvc.perform(get("/buildings/999"))
        .andExpect(status().isNotFound())
        .andDo(print());
  }

  @Test
  @DisplayName("클러스터 데이터 조회 성공")
  void successGetClusters() throws Exception {
    ClusterResponse response = ClusterResponse.builder()
        .clusters(List.of(
            ClusterResponse.ClusterPoint.builder()
                .centerLat(35.88).centerLng(128.60)
                .count(5).avgHriScore(40.0).dominantGrade("CAUTION")
                .build()
        )).build();
    given(buildingService.getClusters(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyInt()))
        .willReturn(response);

    mockMvc.perform(get("/buildings/clusters")
            .param("swLat", "35.8")
            .param("swLng", "128.5")
            .param("neLat", "35.95")
            .param("neLng", "128.7")
            .param("zoomLevel", "12"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.clusters[0].count").value(5))
        .andDo(print());
  }

  @Test
  @DisplayName("클러스터 데이터 조회 실패 - 필수 파라미터 누락")
  void failGetClustersMissingParam() throws Exception {
    mockMvc.perform(get("/buildings/clusters")
            .param("swLat", "35.8"))
        .andExpect(status().isBadRequest())
        .andDo(print());
  }
}