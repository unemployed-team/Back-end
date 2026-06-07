package com.unemployedteam.saferoom.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unemployedteam.saferoom.auth.*;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.user.User;
import com.unemployedteam.saferoom.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthServiceTest {

  @InjectMocks
  private AuthService authService;

  @Spy
  @SuppressWarnings("unused")
  private ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  private UserRepository userRepository;
  @Mock
  private JwtProvider jwtProvider;
  @Mock
  private RedisTokenService redisTokenService;
  @Mock
  private RestTemplate restTemplate;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(authService, "kakaoTokenUri",
        "https://kauth.kakao.com/oauth/token");
    ReflectionTestUtils.setField(authService, "kakaoResourceUri",
        "https://kapi.kakao.com/v2/user/me");
    ReflectionTestUtils.setField(authService, "googleTokenUri",
        "https://oauth2.googleapis.com/token");
    ReflectionTestUtils.setField(authService, "googleResourceUri",
        "https://www.googleapis.com/oauth2/v2/userinfo");
  }

  @Test
  @DisplayName("로그아웃 성공")
  void successLogout() {
    Authentication auth = mock(Authentication.class);
    given(auth.getPrincipal()).willReturn(1L);
    assertDoesNotThrow(() -> authService.logout(auth));
    verify(redisTokenService).deleteRefreshToken(1L);
  }

  @Test
  @DisplayName("로그아웃 실패 - 인증 정보 없음")
  void failLogout() {
    assertThrows(CustomException.class, () -> authService.logout(null));
  }

  @Test
  @DisplayName("토큰 재발급 성공")
  void successReissue() {
    RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");

    given(jwtProvider.getUserId(request.refreshToken())).willReturn(1L);
    given(redisTokenService.isValid(1L, request.refreshToken())).willReturn(true);
    given(jwtProvider.createAccessToken(1L)).willReturn("access");

    TokenResponse result = authService.reissue(request.refreshToken());

    assertNotNull(result);
    assertEquals("access", result.getAccessToken());
  }

  @Test
  @DisplayName("재발급 실패 - 유효하지 않은 토큰")
  void failReissue() {
    given(jwtProvider.getUserId("token")).willReturn(1L);
    given(redisTokenService.isValid(1L, "token")).willReturn(false);
    assertThrows(CustomException.class, () -> authService.reissue("token"));
  }

  @Test
  @DisplayName("카카오 로그인 성공")
  void successKakaoLogin() {
    User mockUser = User.builder().id(1L).oauthId("123").email("dev@com").build();

    given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
        .willReturn(ResponseEntity.ok("{\"access_token\":\"token\"}"));
    given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
        .willReturn(
            ResponseEntity.ok("{\"id\":\"123\", \"kakao_account\":{\"email\":\"dev@com\"}}"));

    given(userRepository.findByOauthProviderAndOauthId(any(), any())).willReturn(Optional.empty());
    given(userRepository.save(any(User.class))).willReturn(mockUser); // save 호출 대응

    given(jwtProvider.createAccessToken(1L)).willReturn("access");
    given(jwtProvider.createRefreshToken(1L)).willReturn("refresh");

    assertDoesNotThrow(() -> authService.kakaoLogin("code"));
  }

  @Test
  @DisplayName("카카오 로그인 실패 - API 통신 오류")
  void failKakaoLogin() {
    given(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
        .willThrow(new RuntimeException("API Error"));

    assertThrows(RuntimeException.class, () -> authService.kakaoLogin("code"));
  }

  @Test
  @DisplayName("구글 로그인 성공")
  void successGoogleLogin() {
    User mockUser = User.builder().id(1L).oauthId("123").email("dev@com").build();

    given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
        .willReturn(ResponseEntity.ok("{\"access_token\":\"mock-token\"}"));
    given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
        .willReturn(ResponseEntity.ok("{\"id\":\"123\", \"email\":\"dev@com\"}"));

    given(userRepository.findByOauthProviderAndOauthId(any(), any())).willReturn(Optional.empty());
    given(userRepository.save(any(User.class))).willReturn(mockUser);

    given(jwtProvider.createAccessToken(1L)).willReturn("access");
    given(jwtProvider.createRefreshToken(1L)).willReturn("refresh");

    assertDoesNotThrow(() -> authService.googleLogin("code"));
  }

  @Test
  @DisplayName("구글 로그인 실패 - API 통신 오류")
  void failGoogleLogin() {
    given(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
        .willThrow(new RuntimeException("API Error"));

    assertThrows(RuntimeException.class, () -> authService.googleLogin("code"));
  }
}