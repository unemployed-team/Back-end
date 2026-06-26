package com.unemployedteam.saferoom.contract;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepositSimulationRepository extends JpaRepository<DepositSimulation, Long> {

  Optional<DepositSimulation> findTopByContractIdOrderByCalculatedAtDesc(Long contractId);
}