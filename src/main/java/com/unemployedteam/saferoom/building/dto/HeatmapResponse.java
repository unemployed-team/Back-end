package com.unemployedteam.saferoom.building.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HeatmapResponse {

  private List<HeatmapPoint> points;

  @Getter
  @Builder
  public static class HeatmapPoint {

    private Double lat;
    private Double lng;
    private Integer score;
    private String riskGrade;
    private Long buildingId;
  }
}