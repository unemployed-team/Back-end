package com.unemployedteam.saferoom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.unemployedteam.saferoom.contract.*;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import com.unemployedteam.saferoom.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ContractController.class)
@Import(GlobalExceptionHandler.class)
public class ContractControllerTest {

  @TestConfiguration
  static class TestSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http
          .csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
      return http.build();
    }
  }

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ContractService contractService;

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  private UsernamePasswordAuthenticationToken getAuth() {
    return new UsernamePasswordAuthenticationToken(1L, null, List.of());
  }

  private ContractResponse sampleContract() {
    return ContractResponse.builder()
        .contractId(1L)
        .buildingId(10L)
        .roadAddress("대구광역시 북구 대학로 80")
        .deposit(50_000_000L)
        .monthlyRent(0L)
        .contractStart(LocalDate.of(2025, 1, 1))
        .contractEnd(LocalDate.of(2027, 1, 1))
        .daysUntilExpiry(365L)
        .expiryAlert(null)
        .createdAt(LocalDateTime.now())
        .build();
  }

  private ContractRequest sampleRequest() {
    return new ContractRequest(10L, 50_000_000L, 0L,
        LocalDate.of(2025, 1, 1), LocalDate.of(2027, 1, 1));
  }

  @Test
  @DisplayName("계약 등록 성공")
  void successRegister() throws Exception {
    given(contractService.register(eq(1L), any(ContractRequest.class)))
        .willReturn(sampleContract());

    mockMvc.perform(post("/contracts")
            .with(authentication(getAuth()))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleRequest())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.contractId").value(1))
        .andExpect(jsonPath("$.deposit").value(50000000))
        .andDo(print());
  }

  @Test
  @DisplayName("계약 등록 실패 - 바디 누락")
  void failRegisterNoBody() throws Exception {
    mockMvc.perform(post("/contracts")
            .with(authentication(getAuth()))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andDo(print());
  }

  @Test
  @DisplayName("내 계약 목록 조회 성공")
  void successGetMyContracts() throws Exception {
    given(contractService.getMyContracts(1L)).willReturn(List.of(sampleContract()));

    mockMvc.perform(get("/contracts/me")
            .with(authentication(getAuth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].contractId").value(1))
        .andDo(print());
  }

  @Test
  @DisplayName("내 계약 목록 조회 실패 - 인증 없음")
  void failGetMyContractsUnauthorized() throws Exception {
    given(contractService.getMyContracts(anyLong()))
        .willThrow(new CustomException(ErrorCode.UNAUTHORIZED_USER));

    mockMvc.perform(get("/contracts/me"))
        .andExpect(status().isUnauthorized())
        .andDo(print());
  }

  @Test
  @DisplayName("계약 수정 성공")
  void successUpdate() throws Exception {
    ContractResponse updated = ContractResponse.builder()
        .contractId(1L).buildingId(10L)
        .roadAddress("대구광역시 북구 대학로 80")
        .deposit(60_000_000L).monthlyRent(0L)
        .contractStart(LocalDate.of(2025, 1, 1))
        .contractEnd(LocalDate.of(2027, 1, 1))
        .daysUntilExpiry(365L).createdAt(LocalDateTime.now())
        .build();

    given(contractService.update(eq(1L), eq(1L), any(ContractRequest.class)))
        .willReturn(updated);

    ContractRequest updateReq = new ContractRequest(10L, 60_000_000L, 0L,
        LocalDate.of(2025, 1, 1), LocalDate.of(2027, 1, 1));

    mockMvc.perform(put("/contracts/1")
            .with(authentication(getAuth()))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateReq)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.deposit").value(60000000))
        .andDo(print());
  }

  @Test
  @DisplayName("계약 수정 실패 - 계약 정보 없음")
  void failUpdateNotFound() throws Exception {
    given(contractService.update(eq(1L), eq(999L), any(ContractRequest.class)))
        .willThrow(new CustomException(ErrorCode.NOT_FOUND_CONTRACT));

    mockMvc.perform(put("/contracts/999")
            .with(authentication(getAuth()))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleRequest())))
        .andExpect(status().isNotFound())
        .andDo(print());
  }

  @Test
  @DisplayName("계약 삭제 성공")
  void successDelete() throws Exception {
    willDoNothing().given(contractService).delete(1L, 1L);

    mockMvc.perform(delete("/contracts/1")
            .with(authentication(getAuth())))
        .andExpect(status().isNoContent())
        .andDo(print());
  }

  @Test
  @DisplayName("계약 삭제 실패 - 계약 정보 없음")
  void failDeleteNotFound() throws Exception {
    willThrow(new CustomException(ErrorCode.NOT_FOUND_CONTRACT))
        .given(contractService).delete(1L, 999L);

    mockMvc.perform(delete("/contracts/999")
            .with(authentication(getAuth())))
        .andExpect(status().isNotFound())
        .andDo(print());
  }

  @Test
  @DisplayName("보증금 회수율 시뮬레이션 성공")
  void successSimulate() throws Exception {
    SimulationResponse sim = SimulationResponse.builder()
        .contractId(1L).deposit(50_000_000L)
        .officialPrice(150_000_000L).priorMortgage(15_000_000L)
        .auctionPrice(108_000_000L).smallTenantProtection(16_500_000L)
        .expectedRecovery(50_000_000L).expectedLoss(0L)
        .recoveryRate(1.0).riskComment("보증금 대부분 회수 가능 (안전)")
        .calculatedAt(LocalDateTime.now())
        .build();

    given(contractService.simulate(1L, 1L)).willReturn(sim);

    mockMvc.perform(post("/contracts/1/simulate")
            .with(authentication(getAuth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.recoveryRate").value(1.0))
        .andExpect(jsonPath("$.riskComment").value("보증금 대부분 회수 가능 (안전)"))
        .andDo(print());
  }

  @Test
  @DisplayName("보증금 시뮬레이션 실패 - 계약 정보 없음")
  void failSimulateNotFound() throws Exception {
    given(contractService.simulate(1L, 999L))
        .willThrow(new CustomException(ErrorCode.NOT_FOUND_CONTRACT));

    mockMvc.perform(post("/contracts/999/simulate")
            .with(authentication(getAuth())))
        .andExpect(status().isNotFound())
        .andDo(print());
  }

  @Test
  @DisplayName("만기 임박 계약 조회 성공")
  void successGetExpiring() throws Exception {
    ContractResponse expiring = ContractResponse.builder()
        .contractId(2L).buildingId(10L)
        .roadAddress("대구광역시 북구 대학로 80")
        .deposit(50_000_000L).monthlyRent(0L)
        .contractStart(LocalDate.now().minusMonths(22))
        .contractEnd(LocalDate.now().plusDays(30))
        .daysUntilExpiry(30L).expiryAlert("D-30")
        .createdAt(LocalDateTime.now())
        .build();

    given(contractService.getExpiringContracts(1L)).willReturn(List.of(expiring));

    mockMvc.perform(get("/contracts/expiring")
            .with(authentication(getAuth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].expiryAlert").value("D-30"))
        .andDo(print());
  }

  @Test
  @DisplayName("만기 임박 계약 조회 실패 - 인증 없음")
  void failGetExpiringUnauthorized() throws Exception {
    given(contractService.getExpiringContracts(anyLong()))
        .willThrow(new CustomException(ErrorCode.UNAUTHORIZED_USER));

    mockMvc.perform(get("/contracts/expiring"))
        .andExpect(status().isUnauthorized())
        .andDo(print());
  }
}