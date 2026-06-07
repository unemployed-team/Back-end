package com.unemployedteam.saferoom.auth;

import com.fasterxml.jackson.databind.JsonNode;

public class GoogleUserInfo implements OAuthUserInfo {

  private final JsonNode root;

  public GoogleUserInfo(JsonNode root) {
    this.root = root;
  }

  @Override
  public String getProvider() {
    return "GOOGLE";
  }

  @Override
  public String getOauthId() {
    return root.path("sub").asText();
  }

  @Override
  public String getEmail() {
    return root.path("email").asText(null);
  }

  @Override
  public String getNickname() {
    return root.path("name").asText(null);
  }
}