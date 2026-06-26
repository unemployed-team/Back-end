package com.unemployedteam.saferoom.fieldreport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FieldReportRequest {
  private String reportType;
  private String description;
}