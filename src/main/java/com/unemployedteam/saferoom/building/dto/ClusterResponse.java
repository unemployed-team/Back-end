package com.unemployedteam.saferoom.building.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ClusterResponse {

  private List<ClusterPoint> clusters;

  @Getter
  @Builder
  public static class ClusterPoint {

    private Double centerLat;
    private Double centerLng;
    private Integer count;
    private Double avgHriScore;
    private String dominantGrade;
    private Long buildingId;
  }
}