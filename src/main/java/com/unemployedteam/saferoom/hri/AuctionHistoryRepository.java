package com.unemployedteam.saferoom.hri;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuctionHistoryRepository extends JpaRepository<AuctionHistory, Long> {

  List<AuctionHistory> findByBuildingIdOrderByCreatedAtDesc(Long buildingId);

  boolean existsByBuildingIdAndAuctionStatus(Long buildingId, String status);
}