package com.unemployedteam.saferoom.user;

import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  public ResponseEntity<UserResponse> getMe(Authentication auth) {
    return ResponseEntity.ok(userService.getCurrentUser(extractUserId(auth)));
  }

  @PatchMapping("/me")
  public ResponseEntity<UserResponse> updateMe(
      Authentication auth,
      @RequestBody UserRequest request) {
    return ResponseEntity.ok(userService.updateUser(extractUserId(auth), request));
  }

  @DeleteMapping("/me")
  public ResponseEntity<Void> deleteMe(Authentication auth) {
    userService.deleteUser(extractUserId(auth));
    return ResponseEntity.noContent().build();
  }

  private Long extractUserId(Authentication auth) {
    if (auth == null) throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
    return (Long) auth.getPrincipal();
  }
}