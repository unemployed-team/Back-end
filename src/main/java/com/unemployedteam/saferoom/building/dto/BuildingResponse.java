package com.unemployedteam.saferoom.building.dto;

import com.unemployedteam.saferoom.building.Building;
import com.unemployedteam.saferoom.hri.HriScore;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BuildingResponse {

  private Long buildingId;
  private String buildingName;
  private String roadAddress;
  private String jibunAddress;
  private Double latitude;
  private Double longitude;
  private String buildingType;
  private Integer buildYear;
  private Boolean isIllegalBuilding;
  private Integer totalScore;
  private String riskGrade;

  public static BuildingResponse from(Building b, HriScore score) {
    return BuildingResponse.builder()
        .buildingId(b.getId())
        .buildingName(b.getBuildingName())
        .roadAddress(b.getRoadAddress())
        .jibunAddress(b.getJibunAddress())
        .latitude(b.getLatitude())
        .longitude(b.getLongitude())
        .buildingType(b.getBuildingType())
        .buildYear(b.getBuildYear())
        .isIllegalBuilding(b.getDetail() != null ? b.getDetail().getIsIllegalBuilding() : null)
        .totalScore(score != null ? score.getTotalScore() : null)
        .riskGrade(score != null ? score.getRiskGrade() : null)
        .build();
  }
}