package com.unemployedteam.saferoom.hri;

import com.unemployedteam.saferoom.building.Building;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "hri_score")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class HriScore {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "report_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "building_id", nullable = false)
  private Building building;

  @Column(name = "total_score", nullable = false)
  private Integer totalScore;

  @Column(name = "building_risk_score")
  private Integer buildingRiskScore;

  @Column(name = "market_risk_score")
  private Integer marketRiskScore;

  @Column(name = "landlord_risk_score")
  private Integer landlordRiskScore;

  @Column(name = "living_risk_score")
  private Integer livingRiskScore;

  @Column(name = "risk_grade", length = 20)
  private String riskGrade;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  public static String calcGrade(int score) {
    if (score >= 70) {
      return "DANGER";
    }
    if (score >= 40) {
      return "CAUTION";
    }
    return "SAFE";
  }
}