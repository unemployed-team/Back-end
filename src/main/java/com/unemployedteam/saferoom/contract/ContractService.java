package com.unemployedteam.saferoom.contract;

import com.unemployedteam.saferoom.building.Building;
import com.unemployedteam.saferoom.building.BuildingRepository;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import com.unemployedteam.saferoom.user.User;
import com.unemployedteam.saferoom.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractService {

  private final ContractRepository contractRepository;
  private final DepositSimulationRepository simulationRepository;
  private final BuildingRepository buildingRepository;
  private final UserRepository userRepository;
  private final DepositSimulator depositSimulator;

  @Transactional
  public ContractResponse register(Long userId, ContractRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
    Building building = buildingRepository.findById(request.getBuildingId())
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_BUILDING));

    Contract contract = Contract.builder()
        .user(user)
        .building(building)
        .deposit(request.getDeposit())
        .monthlyRent(request.getMonthlyRent())
        .contractStart(request.getContractStart())
        .contractEnd(request.getContractEnd())
        .build();

    return ContractResponse.from(contractRepository.save(contract));
  }

  @Transactional(readOnly = true)
  public List<ContractResponse> getMyContracts(Long userId) {
    return contractRepository.findByUserIdOrderByContractEndAsc(userId)
        .stream().map(ContractResponse::from).collect(Collectors.toList());
  }

  @Transactional
  public ContractResponse update(Long userId, Long contractId, ContractRequest request) {
    Contract contract = contractRepository.findByIdAndUserId(contractId, userId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CONTRACT));

    contract.update(request.getDeposit(), request.getMonthlyRent(),
        request.getContractStart(), request.getContractEnd());
    return ContractResponse.from(contract);
  }

  @Transactional
  public void delete(Long userId, Long contractId) {
    Contract contract = contractRepository.findByIdAndUserId(contractId, userId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CONTRACT));
    contractRepository.delete(contract);
  }

  @Transactional
  public SimulationResponse simulate(Long userId, Long contractId) {
    Contract contract = contractRepository.findByIdAndUserId(contractId, userId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CONTRACT));

    Building building = contract.getBuilding();

    long officialPrice = estimateOfficialPrice(building);

    long priorMortgage = (long) (contract.getDeposit() * 0.3); // 임시값

    DepositSimulator.SimulationResult result = depositSimulator.simulate(
        contract.getDeposit(), officialPrice, priorMortgage
    );

    DepositSimulation simulation = DepositSimulation.builder()
        .contract(contract)
        .recoveryRate(result.getRecoveryRate())
        .expectedLoss(result.getExpectedLoss())
        .auctionPrice(result.getAuctionPrice())
        .build();
    DepositSimulation saved = simulationRepository.save(simulation);

    return SimulationResponse.from(saved, contract.getDeposit(),
        officialPrice, priorMortgage, result.getSmallTenantProtection());
  }

  @Transactional(readOnly = true)
  public List<ContractResponse> getExpiringContracts(Long userId) {
    LocalDate now = LocalDate.now();
    LocalDate ninetyDaysLater = now.plusDays(90);
    return contractRepository.findExpiringContracts(userId, now, ninetyDaysLater)
        .stream().map(ContractResponse::from).collect(Collectors.toList());
  }

  private long estimateOfficialPrice(Building building) {
    return 150_000_000L;
  }
}