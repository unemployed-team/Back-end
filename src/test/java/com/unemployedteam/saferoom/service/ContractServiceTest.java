package com.unemployedteam.saferoom.service;

import com.unemployedteam.saferoom.building.Building;
import com.unemployedteam.saferoom.building.BuildingRepository;
import com.unemployedteam.saferoom.contract.*;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import com.unemployedteam.saferoom.hri.OfficialPriceRepository;
import com.unemployedteam.saferoom.user.User;
import com.unemployedteam.saferoom.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class ContractServiceTest {

  @InjectMocks
  private ContractService contractService;

  @Mock
  private ContractRepository contractRepository;
  @Mock
  private DepositSimulationRepository simulationRepository;
  @Mock
  private BuildingRepository buildingRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private OfficialPriceRepository officialPriceRepository;

  @Spy
  private DepositSimulator depositSimulator = new DepositSimulator();

  private User mockUser() {
    return User.builder().id(1L).email("dev@test.com").nickname("테스터").build();
  }

  private Building mockBuilding() {
    return Building.builder()
        .id(10L).pnuCode("2711010100100010000")
        .roadAddress("대구광역시 북구 대학로 80")
        .buildingName("경북대 앞 원룸")
        .latitude(35.88).longitude(128.60)
        .build();
  }

  private ContractRequest sampleRequest() {
    return new ContractRequest(10L, 50_000_000L, 0L,
        LocalDate.of(2025, 1, 1), LocalDate.of(2027, 1, 1));
  }

  private Contract mockContract(User user, Building building) {
    return Contract.builder()
        .id(1L).user(user).building(building)
        .deposit(50_000_000L).monthlyRent(0L)
        .contractStart(LocalDate.of(2025, 1, 1))
        .contractEnd(LocalDate.of(2027, 1, 1))
        .build();
  }

  @Test
  @DisplayName("계약 등록 성공")
  void successRegister() {
    User user = mockUser();
    Building building = mockBuilding();
    Contract saved = mockContract(user, building);

    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    given(buildingRepository.findById(10L)).willReturn(Optional.of(building));
    given(contractRepository.save(any(Contract.class))).willReturn(saved);

    ContractResponse result = contractService.register(1L, sampleRequest());

    assertNotNull(result);
    assertEquals(50_000_000L, result.getDeposit());
  }

  @Test
  @DisplayName("계약 등록 실패 - 사용자 없음")
  void failRegisterUserNotFound() {
    given(userRepository.findById(1L)).willReturn(Optional.empty());

    assertThrows(CustomException.class,
        () -> contractService.register(1L, sampleRequest()));
  }

  @Test
  @DisplayName("내 계약 목록 조회 성공")
  void successGetMyContracts() {
    Building building = mockBuilding();
    User user = mockUser();
    Contract contract = mockContract(user, building);

    given(contractRepository.findByUserIdOrderByContractEndAsc(1L))
        .willReturn(List.of(contract));

    List<ContractResponse> result = contractService.getMyContracts(1L);

    assertFalse(result.isEmpty());
    assertEquals(1L, result.getFirst().getContractId());
  }

  @Test
  @DisplayName("내 계약 목록 조회 성공 - 빈 목록")
  void successGetMyContractsEmpty() {
    given(contractRepository.findByUserIdOrderByContractEndAsc(1L))
        .willReturn(List.of());

    List<ContractResponse> result = contractService.getMyContracts(1L);

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("계약 수정 성공")
  void successUpdate() {
    Building building = mockBuilding();
    User user = mockUser();
    Contract contract = mockContract(user, building);

    given(contractRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(contract));

    ContractRequest updateReq = new ContractRequest(10L, 60_000_000L, 0L,
        LocalDate.of(2025, 1, 1), LocalDate.of(2027, 1, 1));
    ContractResponse result = contractService.update(1L, 1L, updateReq);

    assertNotNull(result);
    assertEquals(60_000_000L, result.getDeposit());
  }

  @Test
  @DisplayName("계약 수정 실패 - 계약 없음")
  void failUpdateNotFound() {
    given(contractRepository.findByIdAndUserId(999L, 1L)).willReturn(Optional.empty());

    assertThrows(CustomException.class,
        () -> contractService.update(1L, 999L, sampleRequest()));
  }

  @Test
  @DisplayName("계약 삭제 성공")
  void successDelete() {
    Building building = mockBuilding();
    User user = mockUser();
    Contract contract = mockContract(user, building);

    given(contractRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(contract));
    willDoNothing().given(contractRepository).delete(contract);

    assertDoesNotThrow(() -> contractService.delete(1L, 1L));
    verify(contractRepository).delete(contract);
  }

  @Test
  @DisplayName("계약 삭제 실패 - 계약 없음")
  void failDeleteNotFound() {
    given(contractRepository.findByIdAndUserId(999L, 1L)).willReturn(Optional.empty());

    CustomException ex = assertThrows(CustomException.class,
        () -> contractService.delete(1L, 999L));
    assertEquals(ErrorCode.NOT_FOUND_CONTRACT, ex.getErrorCode());
  }

  @Test
  @DisplayName("보증금 시뮬레이션 성공 - 소액임차인 범위 내 전세")
  void successSimulateSmallTenant() {
    Building building = mockBuilding();
    User user = mockUser();
    Contract contract = mockContract(user, building);

    given(contractRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(contract));
    given(officialPriceRepository.findByBuildingId(10L)).willReturn(Optional.empty());

    DepositSimulation sim = DepositSimulation.builder()
        .id(1L).contract(contract)
        .recoveryRate(1.0).expectedLoss(0L).auctionPrice(108_000_000L)
        .build();
    given(simulationRepository.save(any(DepositSimulation.class))).willReturn(sim);

    SimulationResponse result = contractService.simulate(1L, 1L);

    assertNotNull(result);
    assertTrue(result.getSmallTenantProtection() > 0);
  }

  @Test
  @DisplayName("보증금 시뮬레이션 실패 - 계약 없음")
  void failSimulateNotFound() {
    // ContractService.simulate(userId=1L, contractId=999L) 내부 호출:
    // contractRepository.findByIdAndUserId(contractId=999L, userId=1L)
    given(contractRepository.findByIdAndUserId(999L, 1L)).willReturn(Optional.empty());

    assertThrows(CustomException.class,
        () -> contractService.simulate(1L, 999L));
  }

  // ── 만기 임박 계약 ───────────────────────────────────────

  @Test
  @DisplayName("만기 임박 계약 조회 성공")
  void successGetExpiringContracts() {
    Building building = mockBuilding();
    User user = mockUser();
    Contract expiring = Contract.builder()
        .id(2L).user(user).building(building)
        .deposit(50_000_000L).monthlyRent(0L)
        .contractStart(LocalDate.now().minusMonths(22))
        .contractEnd(LocalDate.now().plusDays(30))
        .build();

    given(contractRepository.findExpiringContracts(eq(1L), any(), any()))
        .willReturn(List.of(expiring));

    List<ContractResponse> result = contractService.getExpiringContracts(1L);

    assertFalse(result.isEmpty());
    assertEquals("D-30", result.getFirst().getExpiryAlert());
  }

  @Test
  @DisplayName("만기 임박 계약 조회 성공 - 임박 없음")
  void successGetExpiringContractsEmpty() {
    given(contractRepository.findExpiringContracts(eq(1L), any(), any()))
        .willReturn(List.of());

    List<ContractResponse> result = contractService.getExpiringContracts(1L);

    assertTrue(result.isEmpty());
  }
}