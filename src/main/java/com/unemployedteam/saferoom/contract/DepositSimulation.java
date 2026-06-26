package com.unemployedteam.saferoom.contract;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "deposit_recovery_simulation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DepositSimulation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "analysis_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contract_id", nullable = false)
  private Contract contract;

  @Column(name = "recovery_rate", nullable = false)
  private Double recoveryRate;

  @Column(name = "expected_loss", nullable = false)
  private Long expectedLoss;

  @Column(name = "auction_price", nullable = false)
  private Long auctionPrice;

  @CreatedDate
  @Column(name = "calculated_at", updatable = false)
  private LocalDateTime calculatedAt;
}