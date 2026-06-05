package com.unemployedteam.saferoom.auth;

public record RefreshTokenRequest(
    String refreshToken
) {}