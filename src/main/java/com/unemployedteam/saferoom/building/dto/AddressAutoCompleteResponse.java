package com.unemployedteam.saferoom.building.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AddressAutoCompleteResponse {

  private List<AddressItem> items;

  @Getter
  @Builder
  public static class AddressItem {

    private String roadAddress;
    private String jibunAddress;
    private Double lat;
    private Double lng;
    private String pnuCode;
  }
}