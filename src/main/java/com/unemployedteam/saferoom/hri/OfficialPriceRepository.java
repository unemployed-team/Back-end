package com.unemployedteam.saferoom.hri;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OfficialPriceRepository extends JpaRepository<OfficialPrice, Long> {

  Optional<OfficialPrice> findByBuildingId(Long buildingId);
}