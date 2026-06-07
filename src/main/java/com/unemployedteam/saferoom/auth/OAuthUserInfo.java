package com.unemployedteam.saferoom.auth;

public interface OAuthUserInfo {

  String getProvider();

  String getOauthId();

  String getEmail();

  String getNickname();
}