package com.unemployedteam.saferoom.building;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "building_master", indexes = {
    @Index(name = "idx_building_pnu", columnList = "pnu_code"),
    @Index(name = "idx_building_location", columnList = "location")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Building {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "building_id")
  private Long id;

  @Column(name = "pnu_code", nullable = false, unique = true, length = 20)
  private String pnuCode;

  @Column(name = "building_name", length = 200)
  private String buildingName;

  @Column(name = "road_address", nullable = false, length = 300)
  private String roadAddress;

  @Column(name = "jibun_address", length = 300)
  private String jibunAddress;

  @Column(name = "latitude", nullable = false)
  private Double latitude;

  @Column(name = "longitude", nullable = false)
  private Double longitude;

  @Column(name = "location", columnDefinition = "geometry(Point,4326)")
  private Point location;

  @Column(name = "building_type", length = 50)
  private String buildingType;

  @Column(name = "build_year")
  private Integer buildYear;

  @OneToOne(mappedBy = "building", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private BuildingDetail detail;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public void updateLocation(Point point) {
    this.location = point;
  }
}