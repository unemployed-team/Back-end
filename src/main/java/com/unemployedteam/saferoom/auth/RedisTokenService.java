package com.unemployedteam.saferoom.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

  private static final String PREFIX = "refresh:";

  private final StringRedisTemplate redisTemplate;

  public void saveRefreshToken(Long userId, String refreshToken) {
    redisTemplate.opsForValue().set(
        key(userId),
        refreshToken,
        Duration.ofSeconds(JwtProvider.REFRESH_TOKEN_EXPIRE_SECONDS)
    );
  }

  public String getRefreshToken(Long userId) {
    return redisTemplate.opsForValue().get(key(userId));
  }

  public void deleteRefreshToken(Long userId) {
    redisTemplate.delete(key(userId));
  }

  public boolean isValid(Long userId, String refreshToken) {
    String stored = getRefreshToken(userId);
    return refreshToken.equals(stored);
  }

  private String key(Long userId) {
    return PREFIX + userId;
  }
}