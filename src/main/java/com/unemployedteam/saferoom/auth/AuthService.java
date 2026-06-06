package com.unemployedteam.saferoom.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import com.unemployedteam.saferoom.user.User;
import com.unemployedteam.saferoom.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final JwtProvider jwtProvider;
  private final RedisTokenService redisTokenService;
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  @Value("${oauth2.kakao.client-id}")
  private String kakaoClientId;
  @Value("${oauth2.kakao.client-secret}")
  private String kakaoClientSecret;
  @Value("${oauth2.kakao.token-uri}")
  private String kakaoTokenUri;
  @Value("${oauth2.kakao.resource-uri}")
  private String kakaoResourceUri;
  @Value("${oauth2.kakao.redirect-uri}")
  private String kakaoRedirectUri;

  @Value("${oauth2.google.client-id}")
  private String googleClientId;
  @Value("${oauth2.google.client-secret}")
  private String googleClientSecret;
  @Value("${oauth2.google.token-uri}")
  private String googleTokenUri;
  @Value("${oauth2.google.resource-uri}")
  private String googleResourceUri;
  @Value("${oauth2.google.redirect-uri}")
  private String googleRedirectUri;

  @Transactional
  public TokenResponse kakaoLogin(String code) {
    String accessToken = requestAccessToken(kakaoTokenUri,
        buildParams(kakaoClientId, kakaoClientSecret, kakaoRedirectUri, code));
    return processLogin(new KakaoUserInfo(fetchUserInfo(accessToken, kakaoResourceUri)));
  }

  @Transactional
  public TokenResponse googleLogin(String code) {
    String accessToken = requestAccessToken(googleTokenUri,
        buildParams(googleClientId, googleClientSecret, googleRedirectUri, code));
    return processLogin(new GoogleUserInfo(fetchUserInfo(accessToken, googleResourceUri)));
  }

  private String requestAccessToken(String tokenUri, MultiValueMap<String, String> params) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    ResponseEntity<String> response = restTemplate.exchange(tokenUri, HttpMethod.POST,
        new HttpEntity<>(params, headers), String.class);

    try {
      return objectMapper.readTree(response.getBody()).get("access_token").asText();
    } catch (Exception e) {
      throw new CustomException(ErrorCode.OAUTH_TOKEN_REQUEST_FAILED);
    }
  }

  private JsonNode fetchUserInfo(String accessToken, String resourceUri) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);

    ResponseEntity<String> response = restTemplate.exchange(resourceUri, HttpMethod.GET,
        new HttpEntity<>(headers), String.class);

    try {
      return objectMapper.readTree(response.getBody());
    } catch (Exception e) {
      throw new CustomException(ErrorCode.OAUTH_USER_INFO_FAILED);
    }
  }

  private TokenResponse processLogin(OAuthUserInfo info) {
    User user = userRepository.findByOauthProviderAndOauthId(info.getProvider(), info.getOauthId())
        .orElseGet(() -> userRepository.save(User.builder()
            .oauthProvider(info.getProvider())
            .oauthId(info.getOauthId())
            .email(info.getEmail())
            .nickname(info.getNickname())
            .build()));

    String access = jwtProvider.createAccessToken(user.getId());
    String refresh = jwtProvider.createRefreshToken(user.getId());
    redisTokenService.saveRefreshToken(user.getId(), refresh);

    return TokenResponse.builder().accessToken(access).refreshToken(refresh).build();
  }

  private MultiValueMap<String, String> buildParams(String clientId, String clientSecret,
      String redirectUri, String code) {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "authorization_code");
    params.add("client_id", clientId);
    params.add("client_secret", clientSecret);
    params.add("redirect_uri", redirectUri);
    params.add("code", code);
    return params;
  }

  @Transactional
  public TokenResponse reissue(String refreshToken) {
    Long userId = jwtProvider.getUserId(refreshToken);
    if (!redisTokenService.isValid(userId, refreshToken)) {
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    }
    return TokenResponse.builder().accessToken(jwtProvider.createAccessToken(userId))
        .refreshToken(jwtProvider.createRefreshToken(userId)).build();
  }

  public void logout(Long userId) {
    redisTokenService.deleteRefreshToken(userId);
  }
}