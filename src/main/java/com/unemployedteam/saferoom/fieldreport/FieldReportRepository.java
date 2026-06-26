package com.unemployedteam.saferoom.fieldreport;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FieldReportRepository extends JpaRepository<FieldReport, Long> {

  List<FieldReport> findByBuildingIdOrderByCreatedAtDesc(Long buildingId);

  @Query("""
        SELECT f FROM FieldReport f
        WHERE f.building.id = :buildingId AND f.isVerified = true
        """)
  List<FieldReport> findVerifiedByBuildingId(@Param("buildingId") Long buildingId);
}