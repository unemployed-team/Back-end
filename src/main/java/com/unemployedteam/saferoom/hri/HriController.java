package com.unemployedteam.saferoom.hri;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hri")
@RequiredArgsConstructor
public class HriController {

  private final HriService hriService;

  @GetMapping("/{buildingId}/report")
  public ResponseEntity<HriReportResponse> getReport(@PathVariable Long buildingId) {
    return ResponseEntity.ok(hriService.getOrCalculateReport(buildingId));
  }

  @PostMapping("/{buildingId}/recalculate")
  public ResponseEntity<HriReportResponse> recalculate(@PathVariable Long buildingId) {
    return ResponseEntity.ok(hriService.recalculate(buildingId));
  }
}