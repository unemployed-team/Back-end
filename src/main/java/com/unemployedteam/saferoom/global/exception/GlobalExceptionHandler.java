package com.unemployedteam.saferoom.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
    log.error("CustomException: {}", e.getMessage());
    ErrorResponse response = ErrorResponse.builder()
        .status(e.getErrorCode().getStatus().value())
        .message(e.getErrorCode().getMessage())
        .build();
    return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("Unhandled exception: ", e);
    ErrorResponse response = ErrorResponse.builder()
        .status(500)
        .message("서버 내부 오류가 발생했습니다.")
        .build();
    return ResponseEntity.internalServerError().body(response);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
      MissingServletRequestParameterException e) {
    log.warn("파라미터 누락: {}", e.getParameterName());
    ErrorResponse response = ErrorResponse.builder()
        .status(HttpStatus.BAD_REQUEST.value())
        .message("필수 파라미터 '" + e.getParameterName() + "'가 누락되었습니다.")
        .build();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
      HttpMessageNotReadableException e) {
    log.warn("요청 바디 누락 또는 형식 오류: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.builder()
            .status(400)
            .message("요청 본문(Body)이 누락되었거나 형식이 올바르지 않습니다.")
            .build());
  }
}