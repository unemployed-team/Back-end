package com.unemployedteam.saferoom.bookmark;

import com.unemployedteam.saferoom.building.Building;
import com.unemployedteam.saferoom.hri.HriScore;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CompareResponse {

  private List<BuildingCompareItem> buildings;
  private Long safestBuildingId;

  @Getter
  @Builder
  public static class BuildingCompareItem {

    private Long buildingId;
    private String buildingName;
    private String roadAddress;
    private Integer hriScore;
    private String riskGrade;
    private Integer buildingRiskScore;
    private Integer marketRiskScore;
    private Integer landlordRiskScore;
    private Integer livingRiskScore;
    private Long avgDepositPrice;
    private Boolean isHighlighted;

    public static BuildingCompareItem from(Building b, HriScore score,
        Long avgDeposit, boolean highlighted) {
      return BuildingCompareItem.builder()
          .buildingId(b.getId())
          .buildingName(b.getBuildingName())
          .roadAddress(b.getRoadAddress())
          .hriScore(score != null ? score.getTotalScore() : null)
          .riskGrade(score != null ? score.getRiskGrade() : null)
          .buildingRiskScore(score != null ? score.getBuildingRiskScore() : null)
          .marketRiskScore(score != null ? score.getMarketRiskScore() : null)
          .landlordRiskScore(score != null ? score.getLandlordRiskScore() : null)
          .livingRiskScore(score != null ? score.getLivingRiskScore() : null)
          .avgDepositPrice(avgDeposit)
          .isHighlighted(highlighted)
          .build();
    }
  }
}