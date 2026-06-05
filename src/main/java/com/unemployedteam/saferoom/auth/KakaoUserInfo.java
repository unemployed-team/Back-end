package com.unemployedteam.saferoom.auth;

import com.fasterxml.jackson.databind.JsonNode;

public class KakaoUserInfo implements OAuthUserInfo {

  private final JsonNode root;

  public KakaoUserInfo(JsonNode root) {
    this.root = root;
  }

  @Override public String getProvider()  { return "KAKAO"; }
  @Override public String getOauthId()   { return root.path("id").asText(); }
  @Override public String getEmail() {
    return root.path("kakao_account").path("email").asText(null);
  }
  @Override public String getNickname() {
    return root.path("kakao_account").path("profile").path("nickname").asText(null);
  }
}