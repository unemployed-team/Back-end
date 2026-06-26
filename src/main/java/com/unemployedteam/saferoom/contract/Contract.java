package com.unemployedteam.saferoom.contract;

import com.unemployedteam.saferoom.building.Building;
import com.unemployedteam.saferoom.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_asset")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Contract {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "contract_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "building_id", nullable = false)
  private Building building;

  @Column(name = "deposit", nullable = false)
  private Long deposit;

  @Column(name = "monthly_rent")
  private Long monthlyRent;

  @Column(name = "contract_start", nullable = false)
  private LocalDate contractStart;

  @Column(name = "contract_end", nullable = false)
  private LocalDate contractEnd;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  public void update(Long deposit, Long monthlyRent,
      LocalDate contractStart, LocalDate contractEnd) {
    this.deposit = deposit;
    this.monthlyRent = monthlyRent;
    this.contractStart = contractStart;
    this.contractEnd = contractEnd;
  }
}