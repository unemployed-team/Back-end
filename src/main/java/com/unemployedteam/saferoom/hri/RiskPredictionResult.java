package com.unemployedteam.saferoom.hri;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RiskPredictionResult {

  private Long buildingId;
  private Double riskProbability;
  private String riskLevel;
  private String trend;
  private Double currentJeonseRatio;
  private Double predictedJeonseRatio;
}