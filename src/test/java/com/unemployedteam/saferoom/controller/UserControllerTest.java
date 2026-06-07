package com.unemployedteam.saferoom.controller;

import com.unemployedteam.saferoom.user.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private UserService userService;

  @TestConfiguration
  static class TestSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http.csrf(csrf -> csrf.disable());
      return http.build();
    }
  }

  private UsernamePasswordAuthenticationToken getAuth() {
    return new UsernamePasswordAuthenticationToken(1L, null, null);
  }

  @Test
  @DisplayName("내 정보 조회 성공")
  void successGetMe() throws Exception {
    UserResponse response = UserResponse.builder().email("dev.com").nickname("hyun")
        .build();
    given(userService.getCurrentUser(1L)).willReturn(response);

    mockMvc.perform(get("/v1/users/me").with(authentication(getAuth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("dev.com"));
  }

  @Test
  @DisplayName("내 정보 조회 실패 - 인증 없음")
  void failGetMe() throws Exception {
    mockMvc.perform(get("/v1/users/me"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("내 정보 수정 성공")
  void successUpdateMe() throws Exception {
    String json = "{\"nickname\":\"new\"}";
    UserResponse response = UserResponse.builder().nickname("new").build();

    given(userService.updateUser(eq(1L), any(UserRequest.class)))
        .willReturn(response);

    mockMvc.perform(patch("/v1/users/me")
            .with(authentication(getAuth()))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("내 정보 수정 실패 - 인증 없음")
  void failUpdateMe() throws Exception {
    mockMvc.perform(patch("/v1/users/me")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nickname\":\"new\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("회원 탈퇴 성공")
  void successDeleteMe() throws Exception {
    willDoNothing().given(userService).deleteUser(1L);

    mockMvc.perform(delete("/v1/users/me").with(authentication(getAuth())))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("회원 탈퇴 실패 - 인증 없음")
  void failDeleteMe() throws Exception {
    mockMvc.perform(delete("/v1/users/me"))
        .andExpect(status().isUnauthorized());
  }
}