package com.unemployedteam.saferoom.building;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BuildingDetailRepository extends JpaRepository<BuildingDetail, Long> {

  Optional<BuildingDetail> findByBuildingId(Long buildingId);
}