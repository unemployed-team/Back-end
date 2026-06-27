package com.unemployedteam.saferoom.hri;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hri")
@RequiredArgsConstructor
public class HriController {

  private final HriService hriService;
  private final RiskPredictionService riskPredictionService;
  private final CodefAuctionService codefAuctionService;
  private final AuctionHistoryRepository auctionHistoryRepository;
  private final com.unemployedteam.saferoom.building.BuildingRepository buildingRepository;


  @GetMapping("/{buildingId}/report")
  public ResponseEntity<HriReportResponse> getReport(@PathVariable Long buildingId) {
    return ResponseEntity.ok(hriService.getOrCalculateReport(buildingId));
  }

  @PostMapping("/{buildingId}/recalculate")
  public ResponseEntity<HriReportResponse> recalculate(@PathVariable Long buildingId) {
    return ResponseEntity.ok(hriService.recalculate(buildingId));
  }

  @GetMapping("/{buildingId}/prediction")
  public ResponseEntity<RiskPredictionResult> predict(@PathVariable Long buildingId) {
    return ResponseEntity.ok(riskPredictionService.predict(buildingId));
  }

  @PostMapping("/{buildingId}/auction/search")
  public ResponseEntity<AuctionSearchResult> searchAndSaveAuction(
      @PathVariable Long buildingId,
      @RequestBody AuctionSearchRequest request) {

    AuctionSearchResult result = codefAuctionService.searchAuction(
        request.getCourtName(), request.getCaseYear(), request.getCaseNumber());

    com.unemployedteam.saferoom.building.Building building =
        buildingRepository.findById(buildingId)
            .orElseThrow(() -> new com.unemployedteam.saferoom.global.exception.CustomException(
                com.unemployedteam.saferoom.global.exception.ErrorCode.NOT_FOUND_BUILDING));

    auctionHistoryRepository.save(AuctionHistory.builder()
        .building(building)
        .courtName(request.getCourtName())
        .caseNumber(result.getCaseNumber())
        .auctionStatus(result.getStatus())
        .build());

    hriService.recalculate(buildingId);
    return ResponseEntity.ok(result);
  }
}