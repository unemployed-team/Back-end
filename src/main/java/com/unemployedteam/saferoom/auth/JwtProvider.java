package com.unemployedteam.saferoom.auth;

import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

  @Value("${spring.jwt.secret}")
  private String secretKey;

  private static final long ACCESS_TOKEN_EXPIRE_MS = 1000L * 60 * 60 * 24;
  private static final long REFRESH_TOKEN_EXPIRE_MS = 1000L * 60 * 60 * 24 * 7;

  public static final long REFRESH_TOKEN_EXPIRE_SECONDS = 60 * 60 * 24 * 7;

  public String createAccessToken(Long userId) {
    return buildToken(userId, ACCESS_TOKEN_EXPIRE_MS);
  }

  public String createRefreshToken(Long userId) {
    return buildToken(userId, REFRESH_TOKEN_EXPIRE_MS);
  }

  public Long getUserId(String token) {
    try {
      Claims claims = parseClaims(token);
      Object raw = claims.get("user_id");
      if (raw instanceof Number) {
        return ((Number) raw).longValue();
      }
      return Long.parseLong(raw.toString());
    } catch (ExpiredJwtException e) {
      throw new CustomException(ErrorCode.EXPIRED_TOKEN);
    } catch (JwtException | IllegalArgumentException e) {
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    }
  }

  public boolean validate(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (ExpiredJwtException e) {
      throw new CustomException(ErrorCode.EXPIRED_TOKEN);
    } catch (JwtException | IllegalArgumentException e) {
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    }
  }

  private String buildToken(Long userId, long expireMs) {
    Date now = new Date();
    return Jwts.builder()
        .claim("user_id", userId)
        .issuedAt(now)
        .expiration(new Date(now.getTime() + expireMs))
        .signWith(signingKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  private Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(signingKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private SecretKey signingKey() {
    return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
  }
}