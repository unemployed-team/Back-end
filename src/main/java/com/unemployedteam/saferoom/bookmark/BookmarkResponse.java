package com.unemployedteam.saferoom.bookmark;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.unemployedteam.saferoom.hri.HriScore;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BookmarkResponse {

  private Long bookmarkId;
  private Long buildingId;
  private String buildingName;
  private String roadAddress;
  private Integer hriScore;
  private String riskGrade;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
  private LocalDateTime bookmarkedAt;

  public static BookmarkResponse from(Bookmark bookmark, HriScore score) {
    return BookmarkResponse.builder()
        .bookmarkId(bookmark.getId())
        .buildingId(bookmark.getBuilding().getId())
        .buildingName(bookmark.getBuilding().getBuildingName())
        .roadAddress(bookmark.getBuilding().getRoadAddress())
        .hriScore(score != null ? score.getTotalScore() : null)
        .riskGrade(score != null ? score.getRiskGrade() : null)
        .bookmarkedAt(bookmark.getCreatedAt())
        .build();
  }
}