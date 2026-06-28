package com.unemployedteam.saferoom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unemployedteam.saferoom.bookmark.BookmarkController;
import com.unemployedteam.saferoom.bookmark.BookmarkResponse;
import com.unemployedteam.saferoom.bookmark.BookmarkService;
import com.unemployedteam.saferoom.bookmark.CompareResponse;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import com.unemployedteam.saferoom.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookmarkController.class)
@Import({GlobalExceptionHandler.class})
public class BookmarkControllerTest {

  @TestConfiguration
  static class TestSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http
          .csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
      return http.build();
    }
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private BookmarkService bookmarkService;

  private UsernamePasswordAuthenticationToken getAuth() {
    return new UsernamePasswordAuthenticationToken(1L, null, List.of());
  }

  private BookmarkResponse sampleBookmark() {
    return BookmarkResponse.builder()
        .bookmarkId(1L)
        .buildingId(10L)
        .buildingName("테스트빌딩")
        .roadAddress("대구광역시 중구 동성로 1")
        .hriScore(35)
        .riskGrade("SAFE")
        .bookmarkedAt(LocalDateTime.now())
        .build();
  }

  @Test
  @DisplayName("북마크 추가 성공")
  void successAddBookmark() throws Exception {
    given(bookmarkService.addBookmark(1L, 10L)).willReturn(sampleBookmark());

    mockMvc.perform(post("/bookmarks/10")
            .with(authentication(getAuth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bookmarkId").value(1))
        .andExpect(jsonPath("$.riskGrade").value("SAFE"))
        .andDo(print());
  }

  @Test
  @DisplayName("북마크 추가 실패 - 이미 북마크된 건물")
  void failAddBookmarkAlreadyExists() throws Exception {
    given(bookmarkService.addBookmark(1L, 10L))
        .willThrow(new CustomException(ErrorCode.ALREADY_BOOKMARKED));

    mockMvc.perform(post("/bookmarks/10")
            .with(authentication(getAuth())))
        .andExpect(status().isConflict())
        .andDo(print());
  }

  @Test
  @DisplayName("북마크 삭제 성공")
  void successRemoveBookmark() throws Exception {
    willDoNothing().given(bookmarkService).removeBookmark(1L, 10L);

    mockMvc.perform(delete("/bookmarks/10")
            .with(authentication(getAuth())))
        .andExpect(status().isNoContent())
        .andDo(print());
  }

  @Test
  @DisplayName("북마크 삭제 실패 - 존재하지 않는 북마크")
  void failRemoveBookmarkNotFound() throws Exception {
    willThrow(new CustomException(ErrorCode.NOT_FOUND_BOOKMARK))
        .given(bookmarkService).removeBookmark(1L, 10L);

    mockMvc.perform(delete("/bookmarks/10")
            .with(authentication(getAuth())))
        .andExpect(status().isNotFound())
        .andDo(print());
  }

  @Test
  @DisplayName("내 북마크 목록 조회 성공")
  void successGetMyBookmarks() throws Exception {
    given(bookmarkService.getMyBookmarks(1L)).willReturn(List.of(sampleBookmark()));

    mockMvc.perform(get("/bookmarks/me")
            .with(authentication(getAuth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].buildingName").value("테스트빌딩"))
        .andDo(print());
  }

  @Test
  @DisplayName("내 북마크 목록 조회 실패 - 인증 없음")
  void failGetMyBookmarksUnauthorized() throws Exception {
    given(bookmarkService.getMyBookmarks(anyLong()))
        .willThrow(new CustomException(ErrorCode.UNAUTHORIZED_USER));

    mockMvc.perform(get("/bookmarks/me"))
        .andExpect(status().isUnauthorized())
        .andDo(print());
  }

  @Test
  @DisplayName("건물 비교 성공")
  void successCompareBuildings() throws Exception {
    CompareResponse response = CompareResponse.builder()
        .buildings(List.of())
        .safestBuildingId(10L)
        .build();
    given(bookmarkService.compareBuildings(anyList())).willReturn(response);

    mockMvc.perform(post("/bookmarks/compare")
            .contentType(MediaType.APPLICATION_JSON)
            .content("[10, 11, 12]"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.safestBuildingId").value(10))
        .andDo(print());
  }

  @Test
  @DisplayName("건물 비교 실패 - 4개 이상 요청")
  void failCompareBuildingsTooMany() throws Exception {
    given(bookmarkService.compareBuildings(anyList()))
        .willThrow(new CustomException(ErrorCode.INVALID_COMPARE_COUNT));

    mockMvc.perform(post("/bookmarks/compare")
            .contentType(MediaType.APPLICATION_JSON)
            .content("[1, 2, 3, 4]"))
        .andExpect(status().isBadRequest())
        .andDo(print());
  }
}