package com.unemployedteam.saferoom.service;

import com.unemployedteam.saferoom.auth.RedisTokenService;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.user.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @InjectMocks
  private UserService userService;

  @Mock
  private UserRepository userRepository;
  @Mock
  private RedisTokenService redisTokenService;

  private User createMockUser() {
    return User.builder().id(1L).email("dev@com").nickname("hyun").build();
  }

  @Test
  @DisplayName("내 정보 조회 성공")
  void successGetMe() {
    User user = createMockUser();
    given(userRepository.findById(1L)).willReturn(Optional.of(user));

    UserResponse result = userService.getCurrentUser(1L);

    assertEquals("dev@com", result.getEmail());
  }

  @Test
  @DisplayName("내 정보 조회 실패 - 사용자 없음")
  void failGetMe() {
    given(userRepository.findById(1L)).willReturn(Optional.empty());
    assertThrows(CustomException.class, () -> userService.getCurrentUser(1L));
  }

  @Test
  @DisplayName("내 정보 수정 성공")
  void successUpdateMe() {
    User user = createMockUser();
    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    UserRequest request = new UserRequest("jun", "seoul");

    UserResponse result = userService.updateUser(1L, request);

    assertEquals("jun", result.getNickname());
  }

  @Test
  @DisplayName("내 정보 수정 실패 - 사용자 없음")
  void failUpdateMe() {
    given(userRepository.findById(1L)).willReturn(Optional.empty());
    assertThrows(CustomException.class,
        () -> userService.updateUser(1L, new UserRequest("n", "r")));
  }

  @Test
  @DisplayName("회원 탈퇴 성공")
  void successDeleteMe() {
    User user = createMockUser();
    given(userRepository.findById(1L)).willReturn(Optional.of(user));

    assertDoesNotThrow(() -> userService.deleteUser(1L));
    verify(redisTokenService).deleteRefreshToken(1L);
  }

  @Test
  @DisplayName("회원 탈퇴 실패 - 사용자 없음")
  void failDeleteMe() {
    given(userRepository.findById(1L)).willReturn(Optional.empty());
    assertThrows(CustomException.class, () -> userService.deleteUser(1L));
  }
}