package com.unemployedteam.saferoom.user;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  private Long userId;
  private String email;
  private String nickname;
  private String interestRegion;
  private String oauthProvider;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static UserResponse from(User user) {
    return UserResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .interestRegion(user.getInterestRegion())
        .oauthProvider(user.getOauthProvider())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }
}