package com.unemployedteam.saferoom.building;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoAddressClient {

  @Value("${kakao.api.key}")
  private String kakaoApiKey;

  @Value("${kakao.api.address-search-url}")
  private String addressSearchUrl;

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  public List<AddressResult> searchAddress(String query) {
    String url = UriComponentsBuilder.fromHttpUrl(addressSearchUrl)
        .queryParam("query", query)
        .queryParam("size", 10)
        .toUriString();

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "KakaoAK " + kakaoApiKey);

    try {
      ResponseEntity<String> response = restTemplate.exchange(
          url, HttpMethod.GET, new HttpEntity<>(headers), String.class
      );
      JsonNode root = objectMapper.readTree(response.getBody());
      JsonNode documents = root.path("documents");

      List<AddressResult> results = new ArrayList<>();
      for (JsonNode doc : documents) {
        JsonNode addr = doc.path("address");
        JsonNode roadAddr = doc.path("road_address");

        AddressResult result = AddressResult.builder()
            .roadAddress(roadAddr.isMissingNode() ? "" : roadAddr.path("address_name").asText())
            .jibunAddress(addr.isMissingNode() ? "" : addr.path("address_name").asText())
            .lat(Double.parseDouble(doc.path("y").asText("0")))
            .lng(Double.parseDouble(doc.path("x").asText("0")))
            .build();
        results.add(result);
      }
      return results;
    } catch (Exception e) {
      log.error("카카오 주소 검색 실패: {}", e.getMessage());
      return List.of();
    }
  }

  @lombok.Builder
  @lombok.Getter
  public static class AddressResult {

    private String roadAddress;
    private String jibunAddress;
    private Double lat;
    private Double lng;
  }
}