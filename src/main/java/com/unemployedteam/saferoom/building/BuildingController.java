package com.unemployedteam.saferoom.building;

import com.unemployedteam.saferoom.building.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/buildings")
@RequiredArgsConstructor
public class BuildingController {

  private final BuildingService buildingService;

  @GetMapping("/search")
  public ResponseEntity<List<BuildingResponse>> search(
      @RequestParam String keyword) {
    return ResponseEntity.ok(buildingService.searchByKeyword(keyword));
  }

  @GetMapping("/nearby")
  public ResponseEntity<List<BuildingResponse>> nearby(
      @RequestParam double lat,
      @RequestParam double lng,
      @RequestParam(defaultValue = "500") double radius) {
    return ResponseEntity.ok(buildingService.searchNearby(lat, lng, radius));
  }

  @GetMapping("/heatmap")
  public ResponseEntity<HeatmapResponse> heatmap(
      @RequestParam double swLat,
      @RequestParam double swLng,
      @RequestParam double neLat,
      @RequestParam double neLng) {
    return ResponseEntity.ok(buildingService.getHeatmap(swLat, swLng, neLat, neLng));
  }

  @GetMapping("/{buildingId}")
  public ResponseEntity<BuildingResponse> detail(@PathVariable Long buildingId) {
    return ResponseEntity.ok(buildingService.getBuildingDetail(buildingId));
  }
}