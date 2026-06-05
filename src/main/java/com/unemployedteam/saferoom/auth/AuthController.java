package com.unemployedteam.saferoom.auth;

import com.unemployedteam.saferoom.auth.RefreshTokenRequest;
import com.unemployedteam.saferoom.auth.TokenResponse;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @GetMapping("/kakao")
  public ResponseEntity<TokenResponse> kakaoLogin(
      @RequestParam String code) {

    return ResponseEntity.ok(authService.kakaoLogin(code));
  }

  @GetMapping("/google")
  public ResponseEntity<TokenResponse> googleLogin(
      @RequestParam String code) {

    return ResponseEntity.ok(authService.googleLogin(code));
  }

  @PostMapping("/reissue")
  public ResponseEntity<TokenResponse> reissue(
      @RequestBody RefreshTokenRequest request) {
    return ResponseEntity.ok(authService.reissue(request.refreshToken()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(Authentication auth) {
    if (auth == null) throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
    authService.logout((Long) auth.getPrincipal());
    return ResponseEntity.noContent().build();
  }
}