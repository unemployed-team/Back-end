package com.unemployedteam.saferoom.hri;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodefAuctionService {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  @Value("${codef.client-id}")
  private String clientId;

  @Value("${codef.client-secret}")
  private String clientSecret;

  @Value("${codef.search-url}")
  private String searchUrl;

  private static final String TOKEN_URL = "https://oauth.codef.io/oauth/token";

  private String cachedToken = null;
  private long tokenExpiry = 0;

  private String getAccessToken() {
    if (cachedToken != null && System.currentTimeMillis() < tokenExpiry) {
      return cachedToken;
    }
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      headers.setBasicAuth(clientId, clientSecret);
      String body = "grant_type=client_credentials&scope=read";
      ResponseEntity<String> res = restTemplate.exchange(
          TOKEN_URL, HttpMethod.POST,
          new HttpEntity<>(body, headers), String.class);
      JsonNode node = objectMapper.readTree(res.getBody());
      cachedToken = node.get("access_token").asText();
      tokenExpiry = System.currentTimeMillis()
          + (node.get("expires_in").asLong() - 60) * 1000;
      return cachedToken;
    } catch (Exception e) {
      log.error("CODEF 토큰 발급 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.OAUTH_TOKEN_REQUEST_FAILED);
    }
  }

  public AuctionSearchResult searchAuction(String courtName, String caseYear, String caseNumber) {
    try {
      String token = getAccessToken();
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(token);

      Map<String, String> reqBody = new HashMap<>();
      reqBody.put("organization", "0004");
      reqBody.put("courtName", courtName);
      reqBody.put("caseNumberYear", caseYear);
      reqBody.put("caseNumberNumber", caseNumber);

      ResponseEntity<String> res = restTemplate.exchange(
          searchUrl, HttpMethod.POST,
          new HttpEntity<>(reqBody, headers), String.class);

      JsonNode data = objectMapper.readTree(res.getBody()).path("data");

      String resFinalResult = data.path("resFinalResult").asText();
      String status;
      if ("미종국".equals(resFinalResult)) {
        status = "PROCEEDING";
      } else if (resFinalResult.contains("낙찰") || resFinalResult.contains("매각")) {
        status = "COMPLETED";
      } else if (resFinalResult.contains("취하") || resFinalResult.contains("기각")) {
        status = "CANCELLED";
      } else {
        status = "UNKNOWN";
      }

      String address = "";
      JsonNode inventory = data.path("resInventoryList");
      if (inventory.isArray() && inventory.size() > 0) {
        address = inventory.get(0).path("resAddress").asText();
      }

      return AuctionSearchResult.builder()
          .caseNumber(data.path("resCaseNumber").asText())
          .caseName(data.path("resCaseName").asText())
          .finalResult(resFinalResult)
          .status(status)
          .decisionDate(data.path("resDecisionDate").asText())
          .claimAmount(data.path("resClaimAmt").asText())
          .address(address)
          .build();

    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error("경매 조회 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.NOT_FOUND_BUILDING);
    }
  }
}