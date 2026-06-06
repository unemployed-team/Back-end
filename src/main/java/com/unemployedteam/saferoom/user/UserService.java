package com.unemployedteam.saferoom.user;

import com.unemployedteam.saferoom.auth.RedisTokenService;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final RedisTokenService redisTokenService;

  public UserResponse getCurrentUser(Long userId) {
    return UserResponse.from(findActiveUser(userId));
  }

  @Transactional
  public UserResponse updateUser(Long userId, UserRequest request) {
    User user = findActiveUser(userId);
    user.updateProfile(request.getNickname(), request.getInterestRegion());
    return UserResponse.from(user);
  }

  @Transactional
  public void deleteUser(Long userId) {
    User user = findActiveUser(userId);
    redisTokenService.deleteRefreshToken(userId);
    user.softDelete();
  }

  private User findActiveUser(Long userId) {
    return userRepository.findById(userId)
        .filter(u -> u.getDeletedAt() == null)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
  }
}