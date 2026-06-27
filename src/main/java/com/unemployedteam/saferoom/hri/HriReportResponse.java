package com.unemployedteam.saferoom.hri;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class HriReportResponse {

  private Long reportId;
  private Long buildingId;
  private String roadAddress;
  private Integer totalScore;
  private String riskGrade;
  private Integer buildingRiskScore;
  private Integer marketRiskScore;
  private Integer landlordRiskScore;
  private Integer livingRiskScore;
  private List<TradePriceDto> recentTrades;
  private List<AuctionDto> auctions;
  private String shareUrl;
  private LocalDateTime calculatedAt;
  private Double riskProbability;
  private String riskTrend;
  private Double predictedJeonseRatio;
  private Double buildingJeonseRatio;
  private Double districtAvgJeonseRatio;
  private String districtCompareComment;

  @Getter
  @Builder
  public static class TradePriceDto {

    private String tradeType;
    private Long price;
    private String contractYearMonth;

    public static TradePriceDto from(TradePrice t) {
      return TradePriceDto.builder()
          .tradeType(t.getTradeType())
          .price(t.getPrice())
          .contractYearMonth(t.getContractYearMonth())
          .build();
    }
  }

  @Getter
  @Builder
  public static class AuctionDto {

    private String courtName;
    private String caseNumber;
    private String auctionStatus;

    public static AuctionDto from(AuctionHistory a) {
      return AuctionDto.builder()
          .courtName(a.getCourtName())
          .caseNumber(a.getCaseNumber())
          .auctionStatus(a.getAuctionStatus())
          .build();
    }
  }
}