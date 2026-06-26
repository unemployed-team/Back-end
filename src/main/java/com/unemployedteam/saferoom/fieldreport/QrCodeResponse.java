package com.unemployedteam.saferoom.fieldreport;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QrCodeResponse {
  private Long buildingId;
  private String reportUrl;
  private String qrBase64;
}