package com.unemployedteam.saferoom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unemployedteam.saferoom.fieldreport.*;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import com.unemployedteam.saferoom.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FieldReportController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class FieldReportControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private FieldReportService fieldReportService;

  private FieldReportResponse sampleResponse() {
    return FieldReportResponse.builder()
        .fieldReportId(1L)
        .buildingId(10L)
        .reportType("MAJOR_DEFECT")
        .description("누수 심각")
        .isVerified(false)
        .impactScore(0)
        .createdAt(LocalDateTime.now())
        .build();
  }

  @Test
  @DisplayName("현장 제보 제출 성공")
  void successSubmitReport() throws Exception {
    FieldReportRequest request = new FieldReportRequest("MAJOR_DEFECT", "누수 심각");
    given(fieldReportService.submitReport(eq(10L), any(FieldReportRequest.class)))
        .willReturn(sampleResponse());

    mockMvc.perform(post("/field-reports/10/submit")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.reportType").value("MAJOR_DEFECT"))
        .andExpect(jsonPath("$.isVerified").value(false))
        .andDo(print());
  }

  @Test
  @DisplayName("현장 제보 제출 실패 - 존재하지 않는 건물")
  void failSubmitReportBuildingNotFound() throws Exception {
    FieldReportRequest request = new FieldReportRequest("MAJOR_DEFECT", "누수");
    given(fieldReportService.submitReport(eq(999L), any(FieldReportRequest.class)))
        .willThrow(new CustomException(ErrorCode.NOT_FOUND_BUILDING));

    mockMvc.perform(post("/field-reports/999/submit")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andDo(print());
  }

  @Test
  @DisplayName("건물별 제보 목록 조회 성공")
  void successGetReports() throws Exception {
    given(fieldReportService.getReportsByBuilding(10L))
        .willReturn(List.of(sampleResponse()));

    mockMvc.perform(get("/field-reports/10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].fieldReportId").value(1))
        .andExpect(jsonPath("$[0].buildingId").value(10))
        .andDo(print());
  }

  @Test
  @DisplayName("건물별 제보 목록 조회 실패 - 존재하지 않는 건물")
  void failGetReportsBuildingNotFound() throws Exception {
    given(fieldReportService.getReportsByBuilding(999L))
        .willThrow(new CustomException(ErrorCode.NOT_FOUND_BUILDING));

    mockMvc.perform(get("/field-reports/999"))
        .andExpect(status().isNotFound())
        .andDo(print());
  }

  @Test
  @DisplayName("QR 코드 생성 성공")
  void successGetQrCode() throws Exception {
    QrCodeResponse qr = QrCodeResponse.builder()
        .buildingId(10L)
        .reportUrl("http://localhost:8080/field-reports/10/submit")
        .qrBase64("base64encodedimage==")
        .build();
    given(fieldReportService.generateQrCode(10L)).willReturn(qr);

    mockMvc.perform(get("/field-reports/10/qr"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.buildingId").value(10))
        .andExpect(jsonPath("$.qrBase64").value("base64encodedimage=="))
        .andDo(print());
  }

  @Test
  @DisplayName("QR 코드 생성 실패 - 존재하지 않는 건물")
  void failGetQrCodeBuildingNotFound() throws Exception {
    given(fieldReportService.generateQrCode(999L))
        .willThrow(new CustomException(ErrorCode.NOT_FOUND_BUILDING));

    mockMvc.perform(get("/field-reports/999/qr"))
        .andExpect(status().isNotFound())
        .andDo(print());
  }

  @Test
  @DisplayName("제보 검증 성공")
  void successVerifyReport() throws Exception {
    FieldReportResponse verified = FieldReportResponse.builder()
        .fieldReportId(1L).buildingId(10L)
        .reportType("MAJOR_DEFECT").description("누수 심각")
        .isVerified(true).impactScore(6)
        .createdAt(LocalDateTime.now())
        .build();
    given(fieldReportService.verifyReport(1L)).willReturn(verified);

    mockMvc.perform(post("/field-reports/verify/1")
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isVerified").value(true))
        .andExpect(jsonPath("$.impactScore").value(6))
        .andDo(print());
  }

  @Test
  @DisplayName("제보 검증 실패 - 존재하지 않는 제보")
  void failVerifyReportNotFound() throws Exception {
    given(fieldReportService.verifyReport(999L))
        .willThrow(new CustomException(ErrorCode.NOT_FOUND_FIELD_REPORT));

    mockMvc.perform(post("/field-reports/verify/999")
            .with(csrf()))
        .andExpect(status().isNotFound())
        .andDo(print());
  }
}