# SafeRoom — Spring Boot Backend

> **"계약 전에, AI가 위험한 방을 먼저 잡아낸다"**  
> 공공데이터를 분석해 원룸·오피스텔 건물의 주거 위험도를 0~100점으로 산출하는 **청년 주거 안전 플랫폼**

---

##  팀원

### 프론트엔드 개발자

<table>
<tr>
<td align="center">
<img src="https://github.com/seonghyeon-digipen.png" width="100" height="100" alt="이성현">
<br>
<a href="https://github.com/seonghyeon-digipen"><strong>이성현</strong></a>
<br>
<small>프론트엔드 개발자</small>
</td>

<td align="center">
<img src="https://github.com/taejuKwon-digipen.png" width="100" height="100" alt="권태주">
<br>
<a href="https://github.com/taejuKwon-digipen"><strong>권태주</strong></a>
<br>
<small>프론트엔드 개발자</small>
</td>
</tr>
</table>

### 백엔드 개발자

<table>
<tr>
<td align="center">
<img src="https://github.com/Hyun-jun-Lee0811.png" width="100" height="100" alt="이현준">
<br>
<a href="https://github.com/Hyun-jun-Lee0811"><strong>이현준</strong></a>
<br>
<small>백엔드 개발자</small>
</td>
</tr>
</table>

---

## 🛠 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL 15 + PostGIS 3.4 |
| Cache / Session | Redis 7 |
| Auth | JWT (jjwt) + OAuth2 (Kakao, Google) |
| Build | Gradle |
| Infra | AWS EC2, Docker, Docker Compose |
| CI/CD | GitHub Actions |
| Docs | Swagger (SpringDoc OpenAPI 3) |
| Spatial | JTS Topology Suite + PostGIS |

---

##  폴더 구조

```
saferoom/
├── .github/
│   └── workflows/
│       └── deploy.yml                  # GitHub Actions CI/CD
├── src/
│   └── main/
│       ├── java/com/unemployedteam/saferoom/
│       │   ├── auth/                   # 인증 (JWT, OAuth2, Redis 토큰)
│       │   │   ├── AuthController.java
│       │   │   ├── AuthService.java
│       │   │   ├── JwtProvider.java
│       │   │   ├── JwtAuthenticationFilter.java
│       │   │   ├── RedisTokenService.java
│       │   │   ├── KakaoUserInfo.java
│       │   │   ├── GoogleUserInfo.java
│       │   │   ├── OAuthUserInfo.java  (interface)
│       │   │   ├── TokenResponse.java
│       │   │   └── RefreshTokenRequest.java
│       │   ├── building/               # 건물 검색 및 공간 데이터
│       │   │   ├── Building.java       (Entity)
│       │   │   ├── BuildingDetail.java (Entity)
│       │   │   ├── BuildingController.java
│       │   │   ├── BuildingService.java
│       │   │   ├── BuildingRepository.java  (PostGIS 공간쿼리)
│       │   │   ├── BuildingDetailRepository.java
│       │   │   └── dto/
│       │   │       ├── BuildingResponse.java
│       │   │       ├── HeatmapResponse.java
│       │   │       └── ClusterResponse.java
│       │   ├── bookmark/               # 북마크 및 건물 비교
│       │   │   ├── Bookmark.java       (Entity)
│       │   │   ├── BookmarkController.java
│       │   │   ├── BookmarkService.java
│       │   │   ├── BookmarkRepository.java
│       │   │   ├── BookmarkResponse.java
│       │   │   └── CompareResponse.java
│       │   ├── contract/               # 계약 관리 및 경매 배당 시뮬레이터
│       │   │   ├── Contract.java       (Entity)
│       │   │   ├── DepositSimulation.java  (Entity)
│       │   │   ├── ContractController.java
│       │   │   ├── ContractService.java
│       │   │   ├── ContractRepository.java
│       │   │   ├── DepositSimulationRepository.java
│       │   │   ├── DepositSimulator.java   (경매 배당 알고리즘)
│       │   │   ├── ContractRequest.java
│       │   │   ├── ContractResponse.java
│       │   │   └── SimulationResponse.java
│       │   ├── fieldreport/            # QR 현장 제보 크라우드소싱
│       │   │   ├── FieldReport.java    (Entity)
│       │   │   ├── FieldReportController.java
│       │   │   ├── FieldReportService.java
│       │   │   ├── FieldReportRepository.java
│       │   │   ├── QrCodeGenerator.java
│       │   │   ├── FieldReportRequest.java
│       │   │   ├── FieldReportResponse.java
│       │   │   └── QrCodeResponse.java
│       │   ├── hri/                    # HRI Score 산출 및 위험도 예측
│       │   │   ├── HriScore.java       (Entity)
│       │   │   ├── TradePrice.java     (Entity)
│       │   │   ├── AuctionHistory.java (Entity)
│       │   │   ├── OfficialPrice.java  (Entity)
│       │   │   ├── HriController.java
│       │   │   ├── HriService.java
│       │   │   ├── HriCalculator.java  (4대 카테고리 가중합산 알고리즘)
│       │   │   ├── RiskPredictionService.java  (시계열 예측 모델)
│       │   │   ├── CodefAuctionService.java    (CODEF 경매 API 연동)
│       │   │   ├── HriScoreRepository.java
│       │   │   ├── TradePriceRepository.java
│       │   │   ├── AuctionHistoryRepository.java
│       │   │   ├── OfficialPriceRepository.java
│       │   │   ├── HriReportResponse.java
│       │   │   ├── RiskPredictionResult.java
│       │   │   ├── AuctionSearchRequest.java
│       │   │   └── AuctionSearchResult.java
│       │   ├── user/                   # 회원 관리
│       │   │   ├── User.java           (Entity)
│       │   │   ├── UserController.java
│       │   │   ├── UserService.java
│       │   │   ├── UserRepository.java
│       │   │   ├── UserRequest.java
│       │   │   └── UserResponse.java
│       │   └── global/
│       │       ├── config/
│       │       │   ├── AppConfig.java          (RestTemplate, ObjectMapper)
│       │       │   ├── JpaAuditConfig.java
│       │       │   ├── SecurityConfig.java     (JWT 필터 체인)
│       │       │   └── SwaggerConfig.java
│       │       └── exception/
│       │           ├── CustomException.java
│       │           ├── ErrorCode.java
│       │           ├── ErrorResponse.java
│       │           └── GlobalExceptionHandler.java
│       └── resources/
│           └── application.yml
├── src/
│   └── test/
│       └── java/com/unemployedteam/saferoom/
│           ├── controller/
│           │   ├── AuthControllerTest.java
│           │   ├── BookmarkControllerTest.java
│           │   ├── BuildingControllerTest.java
│           │   ├── ContractControllerTest.java
│           │   ├── FieldReportControllerTest.java
│           │   ├── HriControllerTest.java
│           │   └── UserControllerTest.java
│           └── service/
│               ├── AuthServiceTest.java
│               ├── BookmarkServiceTest.java
│               ├── ContractServiceTest.java
│               ├── FieldReportServiceTest.java
│               ├── HriServiceTest.java
│               └── UserServiceTest.java
├── docker-compose.yaml
├── init.sql
└── build.gradle
```

---

##  핵심 도메인 설명

### 1. HRI Score (Housing Risk Index)

원룸·오피스텔의 위험도를 0~100점으로 산출하는 자체 알고리즘입니다. **점수가 높을수록 위험**합니다.

| 카테고리 | 가중치 | 주요 판단 기준 |
|----------|--------|----------------|
| 건축 위험 | 25점 | 위반건축물 여부, 건물 노후도, 가구수 대비 층수 비율 (방 쪼개기 감지) |
| 시세 이상 | 25점 | 전세가율 (보증금 ÷ 매매가) — 90% 이상 시 최고 위험 |
| 임대인 위험 | 30점 | 경매 개시 여부(+30점), 과거 경매 이력(+10점) |
| 생활 안전 | 20점 | 기본 점수 + 현장 제보 가중치 실시간 반영 |

**위험 등급 기준**

| 등급 | 점수 범위 | 의미 |
|------|-----------|------|
| `SAFE` | 0 ~ 39점 | 안전 |
| `CAUTION` | 40 ~ 69점 | 주의 |
| `DANGER` | 70 ~ 100점 | 위험 |

---

### 2. 경매 배당 시뮬레이터

주택임대차보호법을 기반으로 경매 발생 시 **보증금 회수율을 예측**합니다.

```
낙찰가 = 공시가격 × 대구 평균 낙찰가율 (72%)
    ↓
소액임차인 최우선변제 배당
    ↓
선순위 근저당 배당
    ↓
내 보증금 회수액 / 내 보증금 = 회수율 (%)
```

- 대구 기준 소액임차인: 보증금 **5,500만 원 이하** → 최우선 **1,650만 원** 보호
- 위험 코멘트: 90% 이상(안전) / 70 ~ 90%(주의) / 50 ~ 70%(위험) / 50% 미만(매우 위험)

---

### 3. QR 크라우드소싱 현장 제보

건물 현장에 부착된 QR 코드를 스캔하면 **비로그인 상태로도 제보 가능**합니다.  
검증된 제보는 HRI Score에 실시간 가중치로 반영됩니다.

| 제보 유형 | 코드 | 감점 가중치 |
|-----------|------|-------------|
| 임대인 연락 두절 | `LANDLORD_MISSING` | -10점 |
| 등기 변경 징후 | `REGISTRY_CHANGE` | -8점 |
| 중대 하자 (누수/결로) | `MAJOR_DEFECT` | -6점 |
| 관리비 밀실 정산 | `MGMT_FEE` | -3점 |

---

### 4. 공간 데이터 처리 (PostGIS)

건물 위치를 `geometry(Point, 4326)` 타입으로 저장하고 PostGIS 공간 함수로 조회합니다.

- **반경 검색**: `ST_DWithin` — 특정 좌표 기준 반경 N미터 내 건물 검색
- **영역 검색**: `ST_Within` + `ST_MakeEnvelope` — 지도 뷰포트 내 건물 일괄 조회
- **클러스터링**: 줌 레벨에 따라 그리드 기반 클러스터 연산 (줌 15 이상: 개별 핀)
- **공간 인덱스**: `GIST` 인덱스로 대용량 공간쿼리 최적화

---

## 📡 API 명세

### 인증 (`/auth`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/auth/kakao?code=` | 카카오 OAuth2 로그인 |
| GET | `/auth/google?code=` | 구글 OAuth2 로그인 |
| POST | `/auth/reissue` | Access Token 재발급 |
| POST | `/auth/logout` | 로그아웃 (Redis 토큰 삭제) |

### 건물 (`/buildings`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/buildings/search?keyword=` | 주소 키워드 검색 | 
| GET | `/buildings/nearby?lat=&lng=&radius=` | 반경 내 건물 검색 | 
| GET | `/buildings/heatmap?swLat=&swLng=&neLat=&neLng=` | 뷰포트 위험도 히트맵 |
| GET | `/buildings/{buildingId}` | 건물 상세 정보 | 
| GET | `/buildings/clusters?swLat=&swLng=&neLat=&neLng=&zoomLevel=` | 지도 클러스터링 |

### HRI Score (`/hri`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/hri/{buildingId}/report` | HRI 리포트 조회 (없으면 자동 산출) |
| POST | `/hri/{buildingId}/recalculate` | HRI 재산출 | 
| GET | `/hri/{buildingId}/prediction` | 2년 후 보증금 위험 확률 예측 | 
| POST | `/hri/{buildingId}/auction/search` | 경매 정보 조회 및 HRI 반영 | 

### 북마크 (`/bookmarks`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/bookmarks/{buildingId}` | 북마크 추가 |
| DELETE | `/bookmarks/{buildingId}` | 북마크 삭제 | 
| GET | `/bookmarks/me` | 내 북마크 목록 | 
| POST | `/bookmarks/compare` | 최대 3개 건물 안전 지표 비교 | 

### 계약 관리 (`/contracts`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/contracts` | 계약 등록 | 
| GET | `/contracts/me` | 내 계약 목록 | 
| PUT | `/contracts/{contractId}` | 계약 수정 | 
| DELETE | `/contracts/{contractId}` | 계약 삭제 | 
| POST | `/contracts/{contractId}/simulate` | 경매 배당 시뮬레이션 | 
| GET | `/contracts/expiring` | 만기 임박 계약 조회 (D-90 이내) | 

### 현장 제보 (`/field-reports`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/field-reports/{buildingId}/submit` | 현장 제보 제출 (비로그인 가능) | 
| GET | `/field-reports/{buildingId}` | 건물 제보 목록 조회 | 
| GET | `/field-reports/{buildingId}/qr` | QR 코드 Base64 이미지 생성 | 
| POST | `/field-reports/verify/{reportId}` | 제보 검증 (관리자) | 

### 회원 (`/users`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/users/me` | 내 정보 조회 | 
| PATCH | `/users/me` | 닉네임, 관심 지역 수정 | 
| DELETE | `/users/me` | 회원 탈퇴 (Soft Delete) | 

---

##  환경 변수 설정

```env
# Database
DB_USERNAME=saferoom
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret_key_minimum_32_characters

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Kakao OAuth2
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret
KAKAO_REDIRECT_URI=http://localhost:8080/auth/kakao
KAKAO_REST_API_KEY=your_kakao_rest_api_key

# Google OAuth2
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
GOOGLE_REDIRECT_URI=http://localhost:8080/auth/google

# 국토교통부 공공데이터 포털
MOLIT_SERVICE_KEY=your_molit_service_key

# CODEF (법원경매정보)
CODEF_CLIENT_ID=your_codef_client_id
CODEF_CLIENT_SECRET=your_codef_client_secret
```

---

##  로컬 실행

### 요구사항

- Java 21
- Docker & Docker Compose

### 1. DB / Redis 실행

```bash
docker-compose up -d
```

PostgreSQL은 포트 `5433`, Redis는 `6379`로 실행됩니다.  
PostGIS 익스텐션은 `init.sql`에서 자동 설치됩니다.

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 3. Swagger 접속

```
http://localhost:8080/swagger-ui.html
```

---

## 테스트

```bash
./gradlew test
```

컨트롤러 테스트 7개, 서비스 테스트 6개로 각 API와 비즈니스 로직의 성공/실패 케이스를 검증합니다.

```
controller/
  AuthControllerTest          — 카카오/구글 로그인, 토큰 재발급, 로그아웃
  BookmarkControllerTest      — 북마크 추가/삭제/목록/비교
  BuildingControllerTest      — 건물 검색/상세/반경검색/히트맵/클러스터
  ContractControllerTest      — 계약 등록/목록/수정/삭제/시뮬레이션/만기임박
  FieldReportControllerTest   — 제보 등록/조회/QR 생성
  HriControllerTest           — HRI 조회/재산출/예측/경매 조회
  UserControllerTest          — 내정보 조회/수정/탈퇴

service/
  AuthServiceTest             — 로그인/토큰 재발급/로그아웃
  BookmarkServiceTest         — 북마크 관리 및 비교
  ContractServiceTest         — 계약 관리 및 경매 시뮬레이션
  FieldReportServiceTest      — 현장 제보 및 QR 생성
  HriServiceTest              — HRI 계산 및 위험도 예측
  UserServiceTest             — 회원 정보 관리
```

---

##  배포 구조

```
GitHub (develop 브랜치 push)
        ↓ GitHub Actions (.github/workflows/deploy.yml)
Gradle 빌드 (bootJar, 테스트 제외)
        ↓ SCP (appleboy/scp-action)
AWS EC2 /home/ec2-user/saferoom/
        ↓ Docker Compose
PostgreSQL (PostGIS) + Redis + Spring Boot (systemd)
```

`develop` 브랜치에 push하면 자동으로 EC2에 빌드·배포됩니다.  
배포 후 `systemctl is-active saferoom`으로 헬스체크를 수행하며 실패 시 배포가 중단됩니다.

---

##  사용 공공 API

| 기관 | API | 용도 |
|------|-----|------|
| 국토교통부 | 건축물대장정보 서비스 (표제부) | 위반건축물 여부, 층수, 가구수 |
| 국토교통부 | 단독/다가구 전월세 실거래 | 보증금·월세 히스토리 (HRI 시세 이상) |
| 국토교통부 | 오피스텔 전월세 실거래 | 오피스텔 매물 데이터 커버리지 확보 |
| 국토교통부 | 개별공시지가 정보 | 경매 배당 시뮬레이터 공시가격 원천 데이터 |
| 대법원 (CODEF) | 법원경매정보 Open API | 경매 개시 여부 실시간 확인 |
| 카카오 | Maps / 주소검색 API | 주소 자동완성, 좌표 변환, 지도 렌더링 |

---

##  saferoom-batch — 공공데이터 수집 파이프라인

> SafeRoom Spring Boot 서버가 HRI Score를 정확하게 산출하려면 공공 API에서 수집한 데이터가 PostgreSQL에 미리 적재되어 있어야 합니다.  
> **saferoom-batch**는 이 데이터를 수집·정제·적재하는 Python 배치 스크립트 모음입니다.

### 전체 데이터 흐름

```
┌─────────────────────────────────────────────────────────────┐
│                   saferoom-batch (Python)                   │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ 국토교통부 건축물대장 API                                     │
│      ↓ collect_building.py                                  │
│ building_detail                                             │
│ • 위반건축물 여부                                            │
│ • 층수 / 가구수                                              │
│                                                             │
│ 국토교통부 전월세 실거래 API                                  │
│      ↓ collect_trade.py                                     │
│ trade_price                                                 │
│ • 보증금 / 월세                                              │
│ • 계약연월                                                   │
│                                                             │
│ 건물 유형·건축년도 기반 추정                                  │
│      ↓ official_price.py                                    │
│ official_price                                              │
│ • 공시가격 추정                                              │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                  PostgreSQL (saferoom DB)                   │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                  SafeRoom (Spring Boot)                     │
├─────────────────────────────────────────────────────────────┤
│ HriCalculator.calculate(building)                           │
│                                                             │
│ ① building_detail → 건축 위험                                │
│ ② trade_price     → 시세 이상                                │
│ ③ auction_history → 임대인 위험                              │
│ ④ field_report    → 생활 안전                                │
│                                                             │
│ HRI Score (0~100)                                           │
│        ↓                                                    │
│ risk_grade (SAFE / CAUTION / DANGER)                        │
│        ↓                                                    │
│ hri_score 저장                                              │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                     Frontend (React)                        │
├─────────────────────────────────────────────────────────────┤
│ • 위험도 히트맵                                              │
│ • HRI 리포트                                                 │
│ • 경매 배당 시뮬레이션                                        │
│ • 북마크 및 건물 비교                                         │
│ • 계약 만기 알림                                             │
└─────────────────────────────────────────────────────────────┘
```

### 스크립트별 설명

#### `collect_building.py` — 건축물대장 수집

- **API**: 국토교통부 건축물대장정보 서비스 (`getBrTitleInfo`)
- `building_master` 테이블의 전체 건물 도로명 주소를 순회하며 API를 호출합니다.
- 응답에서 위반건축물 여부(`vltnBldYn`), 층수(`grndFlrCnt`), 가구수(`hhldCnt`)를 추출해 `building_detail`에 UPSERT합니다.
- API 응답이 없는 건물은 기본값(층수 4, 가구수 10, 위반 없음)으로 처리합니다.
- **HRI 연관**: `건축 위험` 카테고리 (25점 만점) 산출에 직접 사용됩니다.

#### `collect_trade.py` — 전월세 실거래가 수집

- **API**: 국토교통부 단독/다가구 전월세 + 오피스텔 전월세 실거래
- 대구광역시 7개 구·군 법정동 코드 × 수집 기간 × 2개 유형 = 최대 **84회 API 호출**
- XML 응답을 파싱해 읍면동명(`umdNm`)으로 `building_master`와 매핑한 뒤 `trade_price`에 INSERT합니다.
- 보증금·월세는 만 원 단위를 원 단위(× 10,000)로 변환해 저장합니다.
- **HRI 연관**: `시세 이상` 카테고리 (25점 만점) — 전세가율 계산의 원천 데이터입니다.

#### `official_price.py` — 공시가격 추정값 적재

- 건물 유형(다가구주택 1.2억 / 오피스텔 1.8억)과 건축년도(2000년 이전 ×0.7 / 2015년 이후 ×1.3)를 조합해 공시가격 추정값을 계산합니다.
- `official_price` 테이블에 UPSERT하며 `price_type = 'ESTIMATED'`로 추정값임을 표기합니다.
- **경매 시뮬레이터 연관**: `낙찰가 = 공시가격 × 0.72(대구 평균 낙찰가율)` 계산의 기초 데이터입니다.
