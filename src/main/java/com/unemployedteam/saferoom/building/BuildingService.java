package com.unemployedteam.saferoom.building;

import com.unemployedteam.saferoom.building.dto.*;
import com.unemployedteam.saferoom.hri.HriScore;
import com.unemployedteam.saferoom.hri.HriScoreRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.unemployedteam.saferoom.building.dto.ClusterResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuildingService {

  private final BuildingRepository buildingRepository;
  private final HriScoreRepository hriScoreRepository;

  private final GeometryFactory geometryFactory =
      new GeometryFactory(new PrecisionModel(), 4326);

  @Transactional(readOnly = true)
  public List<BuildingResponse> searchByKeyword(String keyword) {
    List<Building> buildings = buildingRepository
        .findByRoadAddressContainingOrJibunAddressContaining(keyword, keyword);
    return toBuildingResponses(buildings);
  }

  @Transactional(readOnly = true)
  public List<BuildingResponse> searchNearby(double lat, double lng, double radiusMeters) {
    List<Building> buildings = buildingRepository.findNearby(lat, lng, radiusMeters, 50);
    return toBuildingResponses(buildings);
  }

  @Transactional(readOnly = true)
  public HeatmapResponse getHeatmap(double swLat, double swLng, double neLat, double neLng) {
    List<Building> buildings = buildingRepository.findWithinBounds(swLat, swLng, neLat, neLng);

    if (buildings.isEmpty()) {
      return HeatmapResponse.builder().points(List.of()).build();
    }

    List<Long> buildingIds = buildings.stream().map(Building::getId).toList();
    Map<Long, HriScore> scoreMap = hriScoreRepository
        .findLatestByBuildingIds(buildingIds).stream()
        .collect(Collectors.toMap(s -> s.getBuilding().getId(), s -> s));

    List<HeatmapResponse.HeatmapPoint> points = buildings.stream()
        .filter(b -> scoreMap.containsKey(b.getId()))
        .map(b -> {
          HriScore score = scoreMap.get(b.getId());
          return HeatmapResponse.HeatmapPoint.builder()
              .lat(b.getLatitude())
              .lng(b.getLongitude())
              .score(score.getTotalScore())
              .riskGrade(score.getRiskGrade())
              .buildingId(b.getId())
              .build();
        }).collect(Collectors.toList());

    return HeatmapResponse.builder().points(points).build();
  }

  @Transactional(readOnly = true)
  public BuildingResponse getBuildingDetail(Long buildingId) {
    Building building = buildingRepository.findById(buildingId)
        .orElseThrow(() -> new com.unemployedteam.saferoom.global.exception.CustomException(
            com.unemployedteam.saferoom.global.exception.ErrorCode.NOT_FOUND_BUILDING));
    HriScore score = hriScoreRepository.findLatestByBuildingId(buildingId).orElse(null);
    return BuildingResponse.from(building, score);
  }

  @Transactional
  public Building registerBuilding(String roadAddress, String jibunAddress,
      Double lat, Double lng, String pnuCode,
      String buildingType, Integer buildYear) {
    return buildingRepository.findByPnuCode(pnuCode).orElseGet(() -> {
      Point point = geometryFactory.createPoint(new Coordinate(lng, lat));
      Building building = Building.builder()
          .pnuCode(pnuCode)
          .roadAddress(roadAddress)
          .jibunAddress(jibunAddress)
          .latitude(lat)
          .longitude(lng)
          .location(point)
          .buildingType(buildingType)
          .buildYear(buildYear)
          .build();
      return buildingRepository.save(building);
    });
  }

  @Transactional(readOnly = true)
  public ClusterResponse getClusters(double swLat, double swLng,
      double neLat, double neLng, int zoomLevel) {

    List<Building> buildings = buildingRepository.findWithinBounds(swLat, swLng, neLat, neLng);
    if (buildings.isEmpty()) {
      return ClusterResponse.builder().clusters(List.of()).build();
    }

    List<Long> ids = buildings.stream().map(Building::getId).toList();
    Map<Long, HriScore> scoreMap = hriScoreRepository.findLatestByBuildingIds(ids).stream()
        .collect(Collectors.toMap(s -> s.getBuilding().getId(), s -> s));

    double gridSize = zoomLevel >= 15 ? 0.002 : zoomLevel >= 13 ? 0.01
        : zoomLevel >= 11 ? 0.05 : 0.2;

    Map<String, List<Building>> grid = new java.util.HashMap<>();
    for (Building b : buildings) {
      String key = Math.round(b.getLatitude() / gridSize) + ":"
          + Math.round(b.getLongitude() / gridSize);
      grid.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(b);
    }

    List<ClusterResponse.ClusterPoint> clusters = grid.values().stream().map(group -> {
      double avgLat = group.stream().mapToDouble(Building::getLatitude).average().orElse(0);
      double avgLng = group.stream().mapToDouble(Building::getLongitude).average().orElse(0);
      List<HriScore> scores = group.stream()
          .map(b -> scoreMap.get(b.getId())).filter(s -> s != null).toList();
      double avgScore = scores.stream().mapToInt(HriScore::getTotalScore).average().orElse(0);
      String dominant = scores.stream()
          .collect(Collectors.groupingBy(HriScore::getRiskGrade, Collectors.counting()))
          .entrySet().stream().max(Map.Entry.comparingByValue())
          .map(Map.Entry::getKey).orElse("UNKNOWN");
      return ClusterResponse.ClusterPoint.builder()
          .centerLat(avgLat).centerLng(avgLng).count(group.size())
          .avgHriScore((double) Math.round(avgScore))
          .dominantGrade(dominant)
          .buildingId(group.size() == 1 ? group.get(0).getId() : null)
          .build();
    }).toList();

    return ClusterResponse.builder().clusters(clusters).build();
  }

  private List<BuildingResponse> toBuildingResponses(List<Building> buildings) {
    if (buildings.isEmpty()) {
      return List.of();
    }

    List<Long> ids = buildings.stream().map(Building::getId).toList();
    Map<Long, HriScore> scoreMap = hriScoreRepository
        .findLatestByBuildingIds(ids).stream()
        .collect(Collectors.toMap(s -> s.getBuilding().getId(), s -> s));

    return buildings.stream()
        .map(b -> BuildingResponse.from(b, scoreMap.get(b.getId())))
        .collect(Collectors.toList());
  }
}