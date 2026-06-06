package com.unemployedteam.saferoom.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // Auth
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
  EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
  UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

  // User
  NOT_FOUND_USER(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

  // OAuth
  OAUTH_TOKEN_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "소셜 로그인 토큰 요청에 실패했습니다."),
  OAUTH_USER_INFO_FAILED(HttpStatus.BAD_GATEWAY, "소셜 사용자 정보 조회에 실패했습니다.");

  private final HttpStatus status;
  private final String message;
}