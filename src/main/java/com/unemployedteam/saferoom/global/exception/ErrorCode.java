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
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다."),
  UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

  // User
  NOT_FOUND_USER(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

  // OAuth
  OAUTH_TOKEN_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "소셜 로그인 토큰 요청에 실패했습니다."),
  OAUTH_USER_INFO_FAILED(HttpStatus.BAD_GATEWAY, "소셜 사용자 정보 조회에 실패했습니다."),

  // Building
  NOT_FOUND_BUILDING(HttpStatus.NOT_FOUND, "건물 정보를 찾을 수 없습니다."),

  // Bookmark
  ALREADY_BOOKMARKED(HttpStatus.CONFLICT, "이미 북마크한 건물입니다."),
  NOT_FOUND_BOOKMARK(HttpStatus.NOT_FOUND, "북마크를 찾을 수 없습니다."),
  INVALID_COMPARE_COUNT(HttpStatus.BAD_REQUEST, "비교 건물은 최대 3개까지 가능합니다."),

  // FieldReport
  NOT_FOUND_FIELD_REPORT(HttpStatus.NOT_FOUND, "현장 제보를 찾을 수 없습니다.");

  private final HttpStatus status;
  private final String message;
}