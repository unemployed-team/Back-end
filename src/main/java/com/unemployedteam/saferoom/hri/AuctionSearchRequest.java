package com.unemployedteam.saferoom.hri;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuctionSearchRequest {

  private String courtName;
  private String caseYear;
  private String caseNumber;
}