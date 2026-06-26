package com.unemployedteam.saferoom.bookmark;

import com.unemployedteam.saferoom.building.Building;
import com.unemployedteam.saferoom.building.BuildingRepository;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import com.unemployedteam.saferoom.hri.HriScore;
import com.unemployedteam.saferoom.hri.HriScoreRepository;
import com.unemployedteam.saferoom.hri.TradePriceRepository;
import com.unemployedteam.saferoom.user.User;
import com.unemployedteam.saferoom.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {

  private final BookmarkRepository bookmarkRepository;
  private final BuildingRepository buildingRepository;
  private final UserRepository userRepository;
  private final HriScoreRepository hriScoreRepository;
  private final TradePriceRepository tradePriceRepository;

  @Transactional
  public BookmarkResponse addBookmark(Long userId, Long buildingId) {
    if (bookmarkRepository.existsByUserIdAndBuildingId(userId, buildingId)) {
      throw new CustomException(ErrorCode.ALREADY_BOOKMARKED);
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
    Building building = buildingRepository.findById(buildingId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_BUILDING));

    Bookmark bookmark = Bookmark.builder()
        .user(user)
        .building(building)
        .build();
    Bookmark saved = bookmarkRepository.save(bookmark);

    HriScore score = hriScoreRepository.findLatestByBuildingId(buildingId).orElse(null);
    return BookmarkResponse.from(saved, score);
  }

  @Transactional
  public void removeBookmark(Long userId, Long buildingId) {
    if (!bookmarkRepository.existsByUserIdAndBuildingId(userId, buildingId)) {
      throw new CustomException(ErrorCode.NOT_FOUND_BOOKMARK);
    }
    bookmarkRepository.deleteByUserIdAndBuildingId(userId, buildingId);
  }

  @Transactional(readOnly = true)
  public List<BookmarkResponse> getMyBookmarks(Long userId) {
    List<Bookmark> bookmarks = bookmarkRepository.findByUserIdWithBuilding(userId);
    List<Long> buildingIds = bookmarks.stream()
        .map(b -> b.getBuilding().getId()).toList();

    Map<Long, HriScore> scoreMap = hriScoreRepository
        .findLatestByBuildingIds(buildingIds).stream()
        .collect(Collectors.toMap(s -> s.getBuilding().getId(), s -> s));

    return bookmarks.stream()
        .map(b -> BookmarkResponse.from(b, scoreMap.get(b.getBuilding().getId())))
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public CompareResponse compareBuildings(List<Long> buildingIds) {
    if (buildingIds == null || buildingIds.size() > 3) {
      throw new CustomException(ErrorCode.INVALID_COMPARE_COUNT);
    }

    String sixMonthsAgo = LocalDate.now().minusMonths(6)
        .format(DateTimeFormatter.ofPattern("yyyyMM"));

    List<CompareResponse.BuildingCompareItem> items = new ArrayList<>();
    Long safestId = null;
    int lowestScore = Integer.MAX_VALUE;

    for (Long bid : buildingIds) {
      Building building = buildingRepository.findById(bid)
          .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_BUILDING));
      HriScore score = hriScoreRepository.findLatestByBuildingId(bid).orElse(null);
      Double avgDeposit = tradePriceRepository
          .findAvgPrice(bid, "RENT_DEPOSIT", sixMonthsAgo);

      int currentScore = score != null ? score.getTotalScore() : Integer.MAX_VALUE;
      if (currentScore < lowestScore) {
        lowestScore = currentScore;
        safestId = bid;
      }

      items.add(CompareResponse.BuildingCompareItem.from(
          building, score,
          avgDeposit != null ? avgDeposit.longValue() : null,
          false
      ));
    }

    final Long finalSafestId = safestId;
    List<CompareResponse.BuildingCompareItem> highlighted = items.stream()
        .map(item -> CompareResponse.BuildingCompareItem.builder()
            .buildingId(item.getBuildingId())
            .buildingName(item.getBuildingName())
            .roadAddress(item.getRoadAddress())
            .hriScore(item.getHriScore())
            .riskGrade(item.getRiskGrade())
            .buildingRiskScore(item.getBuildingRiskScore())
            .marketRiskScore(item.getMarketRiskScore())
            .landlordRiskScore(item.getLandlordRiskScore())
            .livingRiskScore(item.getLivingRiskScore())
            .avgDepositPrice(item.getAvgDepositPrice())
            .isHighlighted(item.getBuildingId().equals(finalSafestId))
            .build()
        ).collect(Collectors.toList());

    return CompareResponse.builder()
        .buildings(highlighted)
        .safestBuildingId(safestId)
        .build();
  }
}