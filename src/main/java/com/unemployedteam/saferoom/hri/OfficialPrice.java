package com.unemployedteam.saferoom.hri;

import com.unemployedteam.saferoom.building.Building;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "official_price")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OfficialPrice {

  @Id
  @Column(name = "building_id")
  private Long buildingId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "building_id")
  private Building building;

  @Column(name = "official_price", nullable = false)
  private Long officialPrice;

  @Column(name = "price_type", length = 10)
  private String priceType;

  @Column(name = "base_year", length = 4)
  private String baseYear;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}