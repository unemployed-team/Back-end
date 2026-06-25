package com.unemployedteam.saferoom.building.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BuildingSearchRequest {

  private String keyword;
  private Double lat;
  private Double lng;
  private Double radiusMeters = 500.0;
}