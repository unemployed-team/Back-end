package com.unemployedteam.saferoom.hri;

import com.unemployedteam.saferoom.building.Building;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "trade_price")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TradePrice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "price_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "building_id", nullable = false)
  private Building building;

  @Column(name = "trade_type", length = 20, nullable = false)
  private String tradeType;

  @Column(name = "price", nullable = false)
  private Long price;

  @Column(name = "monthly_rent_amount")
  private Long monthlyRentAmount;

  @Column(name = "contract_year_month", length = 7)
  private String contractYearMonth;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}