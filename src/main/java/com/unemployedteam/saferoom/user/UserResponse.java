package com.unemployedteam.saferoom.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

  private Long userId;
  private String email;
  private String nickname;
  private String interestRegion;
  private String oauthProvider;

  public static UserResponse from(User user) {
    return UserResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .interestRegion(user.getInterestRegion())
        .oauthProvider(user.getOauthProvider())
        .build();
  }
}