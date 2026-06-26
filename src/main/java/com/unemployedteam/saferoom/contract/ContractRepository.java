package com.unemployedteam.saferoom.contract;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {

  List<Contract> findByUserIdOrderByContractEndAsc(Long userId);

  Optional<Contract> findByIdAndUserId(Long contractId, Long userId);

  @Query("""
      SELECT c FROM Contract c
      WHERE c.user.id = :userId
      AND c.contractEnd BETWEEN :from AND :to
      ORDER BY c.contractEnd ASC
      """)
  List<Contract> findExpiringContracts(
      @Param("userId") Long userId,
      @Param("from") LocalDate from,
      @Param("to") LocalDate to
  );
}