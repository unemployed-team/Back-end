package com.unemployedteam.saferoom.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByOauthProviderAndOauthId(String oauthProvider, String oauthId);

  Optional<User> findByEmail(String email);
}