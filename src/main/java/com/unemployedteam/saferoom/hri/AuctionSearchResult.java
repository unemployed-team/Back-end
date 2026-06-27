package com.unemployedteam.saferoom.hri;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuctionSearchResult {

  private String caseNumber;
  private String caseName;
  private String finalResult;
  private String status;
  private String decisionDate;
  private String claimAmount;
  private String address;
}