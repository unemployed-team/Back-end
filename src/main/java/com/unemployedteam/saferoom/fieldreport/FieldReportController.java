package com.unemployedteam.saferoom.fieldreport;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/field-reports")
@RequiredArgsConstructor
public class FieldReportController {

  private final FieldReportService fieldReportService;

  @PostMapping("/{buildingId}/submit")
  public ResponseEntity<FieldReportResponse> submit(
      @PathVariable Long buildingId,
      @RequestBody FieldReportRequest request) {
    return ResponseEntity.ok(fieldReportService.submitReport(buildingId, request));
  }

  @GetMapping("/{buildingId}")
  public ResponseEntity<List<FieldReportResponse>> getReports(
      @PathVariable Long buildingId) {
    return ResponseEntity.ok(fieldReportService.getReportsByBuilding(buildingId));
  }

  @GetMapping("/{buildingId}/qr")
  public ResponseEntity<QrCodeResponse> getQrCode(@PathVariable Long buildingId) {
    return ResponseEntity.ok(fieldReportService.generateQrCode(buildingId));
  }

  @PostMapping("/verify/{reportId}")
  public ResponseEntity<FieldReportResponse> verify(@PathVariable Long reportId) {
    return ResponseEntity.ok(fieldReportService.verifyReport(reportId));
  }
}