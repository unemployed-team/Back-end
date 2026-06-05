package com.unemployedteam.saferoom.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "사용자")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long id;

  @Column(name = "oauth_provider", nullable = false, length = 20)
  private String oauthProvider;

  @Column(name = "oauth_id", nullable = false)
  private String oauthId;

  @Column(name = "email", unique = true)
  private String email;

  @Column(name = "nickname")
  private String nickname;

  @Column(name = "interest_region")
  private String interestRegion;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  public void updateProfile(String nickname, String interestRegion) {
    if (nickname != null && !nickname.isBlank()) {
      this.nickname = nickname;
    }
    if (interestRegion != null && !interestRegion.isBlank()) {
      this.interestRegion = interestRegion;
    }
  }

  public void softDelete() {
    this.deletedAt = LocalDateTime.now();
  }
}