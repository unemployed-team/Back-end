package com.unemployedteam.saferoom.building;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BuildingRepository extends JpaRepository<Building, Long> {

  Optional<Building> findByPnuCode(String pnuCode);

  @Query(value = """
      SELECT b.* FROM building_master b
      WHERE ST_DWithin(
          b.location::geography,
          ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
          :radiusMeters
      )
      ORDER BY ST_Distance(
          b.location::geography,
          ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
      )
      LIMIT :limit
      """, nativeQuery = true)
  List<Building> findNearby(
      @Param("lat") double lat,
      @Param("lng") double lng,
      @Param("radiusMeters") double radiusMeters,
      @Param("limit") int limit
  );

  @Query(value = """
      SELECT b.* FROM building_master b
      WHERE ST_Within(
          b.location,
          ST_MakeEnvelope(:swLng, :swLat, :neLng, :neLat, 4326)
      )
      """, nativeQuery = true)
  List<Building> findWithinBounds(
      @Param("swLat") double swLat,
      @Param("swLng") double swLng,
      @Param("neLat") double neLat,
      @Param("neLng") double neLng
  );

  List<Building> findByRoadAddressContainingOrJibunAddressContaining(
      String roadKeyword, String jibunKeyword
  );
}