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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuildingService {

  private final BuildingRepository buildingRepository;
  private final HriScoreRepository hriScoreRepository;
  private final KakaoAddressClient kakaoAddressClient;

  private final GeometryFactory geometryFactory =
      new GeometryFactory(new PrecisionModel(), 4326);

  public AddressAutoCompleteResponse autoComplete(String keyword) {
    List<KakaoAddressClient.AddressResult> kakaoResults =
        kakaoAddressClient.searchAddress(keyword);

    List<AddressAutoCompleteResponse.AddressItem> items = kakaoResults.stream()
        .map(r -> AddressAutoCompleteResponse.AddressItem.builder()
            .roadAddress(r.getRoadAddress())
            .jibunAddress(r.getJibunAddress())
            .lat(r.getLat())
            .lng(r.getLng())
            .build())
        .collect(Collectors.toList());

    return AddressAutoCompleteResponse.builder().items(items).build();
  }

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

  private List<BuildingResponse> toBuildingResponses(List<Building> buildings) {
    List<Long> ids = buildings.stream().map(Building::getId).toList();
    Map<Long, HriScore> scoreMap = hriScoreRepository
        .findLatestByBuildingIds(ids).stream()
        .collect(Collectors.toMap(s -> s.getBuilding().getId(), s -> s));

    return buildings.stream()
        .map(b -> BuildingResponse.from(b, scoreMap.get(b.getId())))
        .collect(Collectors.toList());
  }
}