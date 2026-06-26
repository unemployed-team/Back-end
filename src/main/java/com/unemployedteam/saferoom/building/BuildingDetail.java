package com.unemployedteam.saferoom.building;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "building_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BuildingDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "detail_id")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "building_id")
  private Building building;

  @Column(name = "main_purpose", length = 100)
  private String mainPurpose;

  @Column(name = "structure_type", length = 100)
  private String structureType;

  @Column(name = "floor_count")
  private Integer floorCount;

  @Column(name = "household_count")
  private Integer householdCount;

  @Column(name = "is_illegal_building")
  private Boolean isIllegalBuilding;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}