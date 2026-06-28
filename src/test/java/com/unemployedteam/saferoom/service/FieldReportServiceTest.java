package com.unemployedteam.saferoom.service;

import com.unemployedteam.saferoom.building.BuildingRepository;
import com.unemployedteam.saferoom.fieldreport.FieldReportRepository;
import com.unemployedteam.saferoom.fieldreport.QrCodeGenerator;
import com.unemployedteam.saferoom.hri.HriService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.unemployedteam.saferoom.building.Building;
import com.unemployedteam.saferoom.fieldreport.FieldReport;
import com.unemployedteam.saferoom.fieldreport.FieldReportRequest;
import com.unemployedteam.saferoom.fieldreport.FieldReportResponse;
import com.unemployedteam.saferoom.fieldreport.FieldReportService;
import com.unemployedteam.saferoom.fieldreport.QrCodeResponse;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import com.unemployedteam.saferoom.hri.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class FieldReportServiceTest {

  @InjectMocks
  private FieldReportService fieldReportService;

  @Mock
  private FieldReportRepository fieldReportRepository;
  @Mock
  private BuildingRepository buildingRepository;
  @Mock
  private QrCodeGenerator qrCodeGenerator;
  @Mock
  private HriService hriService;

  private Building mockBuilding() {
    return Building.builder()
        .id(10L).pnuCode("2711010100100010000")
        .roadAddress("대구광역시 북구 대학로 80")
        .buildingName("경북대 앞 원룸")
        .latitude(35.88).longitude(128.60)
        .build();
  }

  private FieldReport mockReport(Building building) {
    return FieldReport.builder()
        .id(1L).building(building)
        .reportType("MAJOR_DEFECT").description("누수 심각")
        .isVerified(false).impactScore(0)
        .build();
  }

  @Test
  @DisplayName("현장 제보 제출 성공")
  void successSubmitReport() {
    Building building = mockBuilding();
    FieldReport report = mockReport(building);
    FieldReportRequest request = new FieldReportRequest("MAJOR_DEFECT", "누수 심각");

    given(buildingRepository.findById(10L)).willReturn(Optional.of(building));
    given(fieldReportRepository.save(any(FieldReport.class))).willReturn(report);
    given(fieldReportRepository.findVerifiedByBuildingId(10L)).willReturn(List.of());

    FieldReportResponse result = fieldReportService.submitReport(10L, request);

    assertNotNull(result);
    assertEquals("MAJOR_DEFECT", result.getReportType());
    assertFalse(result.getIsVerified());
  }

  @Test
  @DisplayName("현장 제보 제출 실패 - 건물 없음")
  void failSubmitReportBuildingNotFound() {
    given(buildingRepository.findById(999L)).willReturn(Optional.empty());

    assertThrows(CustomException.class,
        () -> fieldReportService.submitReport(999L,
            new FieldReportRequest("MAJOR_DEFECT", "누수")));
  }

  @Test
  @DisplayName("제보 검증 성공 - impactScore 자동 부여")
  void successVerifyReport() {
    Building building = mockBuilding();
    FieldReport report = mockReport(building);

    given(fieldReportRepository.findById(1L)).willReturn(Optional.of(report));
    given(fieldReportRepository.save(any(FieldReport.class))).willReturn(report);
    given(hriService.recalculate(10L)).willReturn(null);

    FieldReportResponse result = fieldReportService.verifyReport(1L);

    assertNotNull(result);
    verify(hriService).recalculate(10L);
  }

  @Test
  @DisplayName("제보 검증 실패 - 제보 없음")
  void failVerifyReportNotFound() {
    given(fieldReportRepository.findById(999L)).willReturn(Optional.empty());

    CustomException ex = assertThrows(CustomException.class,
        () -> fieldReportService.verifyReport(999L));
    assertEquals(ErrorCode.NOT_FOUND_FIELD_REPORT, ex.getErrorCode());
  }

  @Test
  @DisplayName("건물별 제보 목록 조회 성공")
  void successGetReportsByBuilding() {
    Building building = mockBuilding();
    given(fieldReportRepository.findByBuildingIdOrderByCreatedAtDesc(10L))
        .willReturn(List.of(mockReport(building)));

    List<FieldReportResponse> result = fieldReportService.getReportsByBuilding(10L);

    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("건물별 제보 목록 조회 성공 - 빈 목록")
  void successGetReportsByBuildingEmpty() {
    given(fieldReportRepository.findByBuildingIdOrderByCreatedAtDesc(10L))
        .willReturn(List.of());

    List<FieldReportResponse> result = fieldReportService.getReportsByBuilding(10L);

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("QR 코드 생성 성공")
  void successGenerateQrCode() {
    Building building = mockBuilding();
    given(buildingRepository.findById(10L)).willReturn(Optional.of(building));
    given(qrCodeGenerator.generateBase64QrCode(anyString(), anyInt(), anyInt()))
        .willReturn("base64data==");

    QrCodeResponse result = fieldReportService.generateQrCode(10L);

    assertNotNull(result);
    assertEquals(10L, result.getBuildingId());
    assertEquals("base64data==", result.getQrBase64());
  }

  @Test
  @DisplayName("QR 코드 생성 실패 - 건물 없음")
  void failGenerateQrCodeBuildingNotFound() {
    given(buildingRepository.findById(999L)).willReturn(Optional.empty());

    assertThrows(CustomException.class,
        () -> fieldReportService.generateQrCode(999L));
  }
}