package com.unemployedteam.saferoom.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

  List<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId);

  Optional<Bookmark> findByUserIdAndBuildingId(Long userId, Long buildingId);

  boolean existsByUserIdAndBuildingId(Long userId, Long buildingId);

  void deleteByUserIdAndBuildingId(Long userId, Long buildingId);

  @Query("""
      SELECT b FROM Bookmark b
      JOIN FETCH b.building bl
      WHERE b.user.id = :userId
      ORDER BY b.createdAt DESC
      """)
  List<Bookmark> findByUserIdWithBuilding(@Param("userId") Long userId);
}