package com.unemployedteam.saferoom.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContractRequest {

  private Long buildingId;
  private Long deposit;
  private Long monthlyRent;
  private LocalDate contractStart;
  private LocalDate contractEnd;
}