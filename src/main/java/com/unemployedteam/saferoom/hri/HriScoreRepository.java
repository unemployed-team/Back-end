package com.unemployedteam.saferoom.hri;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HriScoreRepository extends JpaRepository<HriScore, Long> {

  @Query("""
      SELECT h FROM HriScore h
      WHERE h.building.id = :buildingId
      ORDER BY h.createdAt DESC
      LIMIT 1
      """)
  Optional<HriScore> findLatestByBuildingId(@Param("buildingId") Long buildingId);

  @Query("""
      SELECT h FROM HriScore h
      WHERE h.building.id IN :buildingIds
      AND h.createdAt = (
          SELECT MAX(h2.createdAt) FROM HriScore h2
          WHERE h2.building.id = h.building.id
      )
      """)
  List<HriScore> findLatestByBuildingIds(@Param("buildingIds") List<Long> buildingIds);
}