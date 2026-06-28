package com.unemployedteam.saferoom.fieldreport;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FieldReportResponse {
  private Long fieldReportId;
  private Long buildingId;
  private String reportType;
  private String description;
  private Boolean isVerified;
  private Integer impactScore;
  private LocalDateTime createdAt;

  public static FieldReportResponse from(FieldReport r) {
    return FieldReportResponse.builder()
        .fieldReportId(r.getId())
        .buildingId(r.getBuilding().getId())
        .reportType(r.getReportType())
        .description(r.getDescription())
        .isVerified(r.getIsVerified())
        .impactScore(r.getImpactScore())
        .createdAt(r.getCreatedAt())
        .build();
  }
}