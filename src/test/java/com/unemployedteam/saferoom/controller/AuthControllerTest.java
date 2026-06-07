package com.unemployedteam.saferoom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unemployedteam.saferoom.auth.AuthController;
import com.unemployedteam.saferoom.auth.AuthService;
import com.unemployedteam.saferoom.auth.RefreshTokenRequest;
import com.unemployedteam.saferoom.auth.TokenResponse;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import com.unemployedteam.saferoom.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private AuthService authService;

  @Test
  @DisplayName("카카오 로그인 성공 시 토큰 반환 - 성공")
  void successKakaoLogin() throws Exception {
    TokenResponse response = new TokenResponse("access", "refresh");
    given(authService.kakaoLogin("test-code")).willReturn(response);

    mockMvc.perform(get("/auth/kakao")
            .param("code", "test-code"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access"))
        .andDo(print());
  }

  @Test
  @DisplayName("카카오 로그인 코드 누락 시 400 에러 - 실패")
  void failKakaoLogin() throws Exception {
    mockMvc.perform(get("/auth/kakao"))
        .andExpect(status().isBadRequest())
        .andDo(print());
  }

  @Test
  @DisplayName("구글 로그인 성공 시 토큰 반환 - 성공")
  void successGoogleLogin() throws Exception {
    TokenResponse response = new TokenResponse("access", "refresh");
    given(authService.googleLogin("code")).willReturn(response);

    mockMvc.perform(get("/auth/google")
            .param("code", "code"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access"))
        .andDo(print());
  }

  @Test
  @DisplayName("토큰 재발급 요청 성공 시 새 토큰 반환 - 성공")
  void successReissue() throws Exception {
    RefreshTokenRequest request = new RefreshTokenRequest("refresh");
    TokenResponse response = new TokenResponse("new-access", "new-refresh");

    given(authService.reissue("refresh")).willReturn(response);

    mockMvc.perform(post("/auth/reissue")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("new-access"))
        .andDo(print());
  }

  @Test
  @DisplayName("토큰 재발급 요청 시 바디가 없으면 400 에러 - 실패")
  void failReissue() throws Exception {
    mockMvc.perform(post("/auth/reissue")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)) // 바디 누락
        .andExpect(status().isBadRequest())
        .andDo(print());
  }

  @Test
  @WithMockUser
  @DisplayName("로그아웃 성공 시 204 반환 - 성공")
  void successLogout() throws Exception {
    doNothing().when(authService).logout(any(Authentication.class));

    mockMvc.perform(post("/auth/logout")
            .with(csrf()))
        .andExpect(status().isNoContent())
        .andDo(print());
  }

  @Test
  @WithAnonymousUser
  @DisplayName("로그아웃 요청 시 인증 헤더가 없으면 401 에러 - 실패")
  void failLogout() throws Exception {
    org.mockito.BDDMockito.willThrow(new CustomException(ErrorCode.UNAUTHORIZED))
        .given(authService).logout(any());

    mockMvc.perform(post("/auth/logout")
            .with(csrf()))
        .andExpect(status().isUnauthorized())
        .andDo(print());
  }
}