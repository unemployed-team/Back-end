package com.unemployedteam.saferoom.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
}