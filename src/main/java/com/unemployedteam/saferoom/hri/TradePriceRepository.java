package com.unemployedteam.saferoom.hri;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TradePriceRepository extends JpaRepository<TradePrice, Long> {

  @Query("""
      SELECT AVG(t.price) FROM TradePrice t
      WHERE t.building.id = :buildingId
      AND t.tradeType = :tradeType
      AND t.contractYearMonth >= :fromYearMonth
      """)
  Double findAvgPrice(@Param("buildingId") Long buildingId,
      @Param("tradeType") String tradeType,
      @Param("fromYearMonth") String fromYearMonth);

  @Query("""
      SELECT t FROM TradePrice t
      WHERE t.building.id = :buildingId
      ORDER BY t.contractYearMonth DESC
      LIMIT 12
      """)
  List<TradePrice> findRecent12Months(@Param("buildingId") Long buildingId);

  @Query("""
    SELECT AVG(t.price) FROM TradePrice t
    WHERE t.building.roadAddress LIKE :districtKeyword
    AND t.tradeType = :tradeType
    AND t.contractYearMonth >= :fromYearMonth
    """)
  Double findDistrictAvgPrice(
      @Param("districtKeyword") String districtKeyword,
      @Param("tradeType") String tradeType,
      @Param("fromYearMonth") String fromYearMonth
  );
}