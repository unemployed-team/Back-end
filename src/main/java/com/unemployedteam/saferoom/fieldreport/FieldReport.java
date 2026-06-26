package com.unemployedteam.saferoom.fieldreport;

import com.unemployedteam.saferoom.building.Building;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "QR 현장 제보")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FieldReport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "field_report_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "building_id", nullable = false)
  private Building building;

  @Column(name = "report_type", length = 50, nullable = false)
  private String reportType;

  @Column(name = "description", length = 1000)
  private String description;

  @Column(name = "is_verified", nullable = false)
  @Builder.Default
  private Boolean isVerified = false;

  @Column(name = "impact_score", nullable = false)
  @Builder.Default
  private Integer impactScore = 0;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  public void verify(int impactScore) {
    this.isVerified = true;
    this.impactScore = impactScore;
  }
}