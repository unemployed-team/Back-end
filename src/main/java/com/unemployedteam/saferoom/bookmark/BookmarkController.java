package com.unemployedteam.saferoom.bookmark;

import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

  private final BookmarkService bookmarkService;

  @PostMapping("/{buildingId}")
  public ResponseEntity<BookmarkResponse> add(
      @PathVariable Long buildingId, Authentication auth) {
    return ResponseEntity.ok(bookmarkService.addBookmark(extractUserId(auth), buildingId));
  }

  @DeleteMapping("/{buildingId}")
  public ResponseEntity<Void> remove(
      @PathVariable Long buildingId, Authentication auth) {
    bookmarkService.removeBookmark(extractUserId(auth), buildingId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  public ResponseEntity<List<BookmarkResponse>> myBookmarks(Authentication auth) {
    return ResponseEntity.ok(bookmarkService.getMyBookmarks(extractUserId(auth)));
  }

  @PostMapping("/compare")
  public ResponseEntity<CompareResponse> compare(
      @RequestBody List<Long> buildingIds) {
    return ResponseEntity.ok(bookmarkService.compareBuildings(buildingIds));
  }

  private Long extractUserId(Authentication auth) {
    if (auth == null) throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
    return (Long) auth.getPrincipal();
  }
}