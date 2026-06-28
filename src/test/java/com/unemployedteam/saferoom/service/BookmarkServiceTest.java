package com.unemployedteam.saferoom.service;

import com.unemployedteam.saferoom.bookmark.*;
import com.unemployedteam.saferoom.building.Building;
import com.unemployedteam.saferoom.building.BuildingRepository;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import com.unemployedteam.saferoom.hri.HriScore;
import com.unemployedteam.saferoom.hri.HriScoreRepository;
import com.unemployedteam.saferoom.hri.TradePriceRepository;
import com.unemployedteam.saferoom.user.User;
import com.unemployedteam.saferoom.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class BookmarkServiceTest {

  @InjectMocks
  private BookmarkService bookmarkService;

  @Mock
  private BookmarkRepository bookmarkRepository;
  @Mock
  private BuildingRepository buildingRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private HriScoreRepository hriScoreRepository;
  @Mock
  private TradePriceRepository tradePriceRepository;

  private User mockUser() {
    return User.builder().id(1L).email("dev@test.com").nickname("테스터").build();
  }

  private Building mockBuilding() {
    return Building.builder()
        .id(10L).pnuCode("2711010100100010000")
        .roadAddress("대구광역시 북구 대학로 80")
        .buildingName("경북대 앞 원룸")
        .latitude(35.88).longitude(128.60)
        .build();
  }

  private HriScore mockScore(Building building) {
    return HriScore.builder()
        .building(building).totalScore(35).riskGrade("SAFE").build();
  }

  @Test
  @DisplayName("북마크 추가 성공")
  void successAddBookmark() {
    Building building = mockBuilding();
    User user = mockUser();

    given(bookmarkRepository.existsByUserIdAndBuildingId(1L, 10L)).willReturn(false);
    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    given(buildingRepository.findById(10L)).willReturn(Optional.of(building));

    Bookmark saved = Bookmark.builder().id(1L).user(user).building(building).build();
    given(bookmarkRepository.save(any(Bookmark.class))).willReturn(saved);
    given(hriScoreRepository.findLatestByBuildingId(10L))
        .willReturn(Optional.of(mockScore(building)));

    BookmarkResponse result = bookmarkService.addBookmark(1L, 10L);

    assertNotNull(result);
    assertEquals("SAFE", result.getRiskGrade());
    assertEquals(35, result.getHriScore());
  }

  @Test
  @DisplayName("북마크 추가 실패 - 이미 북마크 존재")
  void failAddBookmarkAlreadyExists() {
    given(bookmarkRepository.existsByUserIdAndBuildingId(1L, 10L)).willReturn(true);

    assertThrows(CustomException.class, () -> bookmarkService.addBookmark(1L, 10L));
  }

  @Test
  @DisplayName("북마크 삭제 성공")
  void successRemoveBookmark() {
    given(bookmarkRepository.existsByUserIdAndBuildingId(1L, 10L)).willReturn(true);
    willDoNothing().given(bookmarkRepository).deleteByUserIdAndBuildingId(1L, 10L);

    assertDoesNotThrow(() -> bookmarkService.removeBookmark(1L, 10L));
    verify(bookmarkRepository).deleteByUserIdAndBuildingId(1L, 10L);
  }

  @Test
  @DisplayName("북마크 삭제 실패 - 북마크 없음")
  void failRemoveBookmarkNotFound() {
    given(bookmarkRepository.existsByUserIdAndBuildingId(1L, 10L)).willReturn(false);

    CustomException ex = assertThrows(CustomException.class,
        () -> bookmarkService.removeBookmark(1L, 10L));
    assertEquals(ErrorCode.NOT_FOUND_BOOKMARK, ex.getErrorCode());
  }

  @Test
  @DisplayName("내 북마크 목록 조회 성공")
  void successGetMyBookmarks() {
    Building building = mockBuilding();
    User user = mockUser();
    Bookmark bookmark = Bookmark.builder().id(1L).user(user).building(building).build();

    given(bookmarkRepository.findByUserIdWithBuilding(1L)).willReturn(List.of(bookmark));
    given(hriScoreRepository.findLatestByBuildingIds(anyList()))
        .willReturn(List.of(mockScore(building)));

    List<BookmarkResponse> result = bookmarkService.getMyBookmarks(1L);

    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals("경북대 앞 원룸", result.getFirst().getBuildingName());
  }

  @Test
  @DisplayName("내 북마크 목록 조회 성공 - 빈 목록")
  void successGetMyBookmarksEmpty() {
    given(bookmarkRepository.findByUserIdWithBuilding(1L)).willReturn(List.of());

    List<BookmarkResponse> result = bookmarkService.getMyBookmarks(1L);

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("건물 비교 성공 - 최저 점수 건물 하이라이트")
  void successCompareBuildings() {
    Building b1 = Building.builder().id(10L).pnuCode("A")
        .roadAddress("대구 북구 대학로 80").buildingName("A빌딩")
        .latitude(35.88).longitude(128.60).build();
    Building b2 = Building.builder().id(11L).pnuCode("B")
        .roadAddress("대구 북구 복현로 1").buildingName("B빌딩")
        .latitude(35.89).longitude(128.61).build();

    given(buildingRepository.findById(10L)).willReturn(Optional.of(b1));
    given(buildingRepository.findById(11L)).willReturn(Optional.of(b2));

    HriScore score1 = HriScore.builder().building(b1).totalScore(60).riskGrade("CAUTION").build();
    HriScore score2 = HriScore.builder().building(b2).totalScore(30).riskGrade("SAFE").build();

    given(hriScoreRepository.findLatestByBuildingId(10L)).willReturn(Optional.of(score1));
    given(hriScoreRepository.findLatestByBuildingId(11L)).willReturn(Optional.of(score2));
    given(tradePriceRepository.findAvgPrice(anyLong(), anyString(), anyString()))
        .willReturn(null);

    CompareResponse result = bookmarkService.compareBuildings(List.of(10L, 11L));

    assertNotNull(result);
    assertEquals(11L, result.getSafestBuildingId());
    result.getBuildings().forEach(item -> {
      if (item.getBuildingId().equals(11L)) {
        assertTrue(item.getIsHighlighted());
      } else {
        assertFalse(item.getIsHighlighted());
      }
    });
  }

  @Test
  @DisplayName("건물 비교 실패 - 4개 이상 요청")
  void failCompareBuildingsTooMany() {
    assertThrows(CustomException.class,
        () -> bookmarkService.compareBuildings(List.of(1L, 2L, 3L, 4L)));
  }
}