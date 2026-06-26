package com.unemployedteam.saferoom.fieldreport;

import com.unemployedteam.saferoom.building.Building;
import com.unemployedteam.saferoom.building.BuildingRepository;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import com.unemployedteam.saferoom.hri.HriService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FieldReportService {

  private final FieldReportRepository fieldReportRepository;
  private final BuildingRepository buildingRepository;
  private final QrCodeGenerator qrCodeGenerator;
  private final HriService hriService;

  @Value("${app.base-url:http://localhost:8080}")
  private String baseUrl;

  private static final Map<String, Integer> IMPACT_SCORE_MAP = Map.of(
      "REGISTRY_CHANGE", 8,
      "MAJOR_DEFECT", 6,
      "LANDLORD_MISSING", 10,
      "MGMT_FEE", 3
  );

  @Transactional
  public FieldReportResponse submitReport(Long buildingId, FieldReportRequest request) {
    Building building = buildingRepository.findById(buildingId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_BUILDING));

    FieldReport report = FieldReport.builder()
        .building(building)
        .reportType(request.getReportType())
        .description(request.getDescription())
        .isVerified(false)
        .impactScore(0)
        .build();

    FieldReport saved = fieldReportRepository.save(report);

    long verifiedCount = fieldReportRepository
        .findVerifiedByBuildingId(buildingId).size();

    if (verifiedCount >= 3) {
      autoVerifyAndRecalculate(saved);
    }

    return FieldReportResponse.from(saved);
  }

  @Transactional
  public FieldReportResponse verifyReport(Long reportId) {
    FieldReport report = fieldReportRepository.findById(reportId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FIELD_REPORT));

    int impact = IMPACT_SCORE_MAP.getOrDefault(report.getReportType(), 3);
    report.verify(impact);
    fieldReportRepository.save(report);

    hriService.recalculate(report.getBuilding().getId());

    return FieldReportResponse.from(report);
  }

  public List<FieldReportResponse> getReportsByBuilding(Long buildingId) {
    return fieldReportRepository.findByBuildingIdOrderByCreatedAtDesc(buildingId)
        .stream().map(FieldReportResponse::from).toList();
  }

  public QrCodeResponse generateQrCode(Long buildingId) {
    buildingRepository.findById(buildingId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_BUILDING));

    String reportUrl = baseUrl + "/v1/field-reports/" + buildingId + "/submit";
    String qrBase64 = qrCodeGenerator.generateBase64QrCode(reportUrl, 300, 300);

    return QrCodeResponse.builder()
        .buildingId(buildingId)
        .reportUrl(reportUrl)
        .qrBase64(qrBase64)
        .build();
  }

  private void autoVerifyAndRecalculate(FieldReport report) {
    int impact = IMPACT_SCORE_MAP.getOrDefault(report.getReportType(), 3);
    report.verify(impact);
    fieldReportRepository.save(report);
    hriService.recalculate(report.getBuilding().getId());
  }
}