## 🔧 HUFJOK 백엔드 트러블슈팅 로그

HUFJOK 백엔드 프로젝트 개발 과정에서 발생한 주요 기술적 문제와 해결 과정을 기록합니다.

---

### 1. 로컬 개발 환경 및 서버 실행 오류

#### 1.1. VS Code 프로젝트 인식 실패 (ConfigError)

* **문제 상황:** VS Code가 `ConfigError: The project 'todolist-BE' is not a valid java project` 에러를 발생시켰다.
* **원인 분석:** `settings.gradle` 파일의 `rootProject.name`이 이전 프로젝트명(`todolist`)으로 설정되어, Java 확장 프로그램이 프로젝트 구조를 잘못 인식했다.
* **최종 해결:** `settings.gradle`의 `rootProject.name`을 `hufjok`으로 수정하고, `Java: Clean Java Language Server Workspace`를 실행하여 VS Code의 Java 캐시를 초기화했다.

#### 1.2. 로컬 Swagger 테스트 불가 (CORS 에러)

* **문제 상황:** 로컬 서버(`localhost:8080`)의 Swagger UI에서 API 테스트 시 `Failed to fetch` 에러가 발생했다.
* **원인 분석:** `SwaggerConfig.java`에 배포 서버 주소(`https://hufjok.lion.it.kr`)만 하드 코딩되어, 로컬 요청이 배포 서버로 전송되다 CORS 정책 위반으로 차단되었다.
* **최종 해결:** `SwaggerConfig.java`에 `.addServersItem(new Server().url("http://localhost:8080"))`를 추가하고, Swagger UI의 [Servers] 드롭다운에서 로컬 주소를 선택하여 테스트 환경을 분리했다.

#### 1.3. 서버 실행 실패 (파일 위치 오류)

* **문제 상황:** 서버 실행 시 `Application run failed (BeanCreationException)`가 발생했다.
* **원인 분석:** `CustomOAuth2UserService.java` 파일이 `config` 패키지에 잘못 위치했다. 파일 상단의 `package security.oauth2` 선언과 실제 파일 시스템 경로가 불일치했다.
* **최종 해결:** 해당 파일을 `config/` 폴더에서 `security/oauth2/` 폴더로 이동시켰다.

#### 1.4. 서버 실행 실패 (설정값 누락)

* **문제 상황:** 서버 실행 시 `PlaceholderResolutionException`이 발생했다.
* **원인 분석:** `AttachmentService`의 `@Value("${file.dir}")`에 주입될 설정값이 `application.yml`에 정의되지 않았다.
* **최종 해결:** `application.yml` 파일에 `file: dir: ./files/` 설정을 추가했다.

---

### 2. API 로직 및 데이터 정합성 오류

#### 2.1. 신규 회원가입 500 에러 / 포인트 이력 누락

* **문제 상황:** 신규 회원 로그인 시 500 에러가 발생하거나, 500 포인트가 지급되어도 `GET /api/v1/users/mypage/points/history` 응답이 빈 배열 `[]`로 반환되었다.
* **원인 분석:**
    1.  `CustomOAuth2UserService`와 `UserService`에서 유저 생성을 중복 시도했다.
    2.  `UserService.saveFirstLogin`의 User 저장 트랜잭션이 커밋되기 전에 `PointService.awardSignupBonus`가 호출되어, `PointHistory` 저장 시 DB 외래 키 참조(FK) 에러가 발생했다.
* **최종 해결:**
    1.  `CustomOAuth2UserService`의 `saveOrUpdate` 호출을 제거하여 유저 생성 로직을 `UserService`로 일원화했다.
    2.  `PointService`의 `awardSignupBonus` 메소드에 `@Transactional(propagation = Propagation.REQUIRES_NEW)`을 추가하여 트랜잭션을 분리, User 저장이 먼저 커밋되도록 보장했다.
    3.  (이후 리팩토링) `UserService`에서 가입 시 `points(500)`을 직접 설정하고, `PointService`에서는 `updatePoints()` 호출 없이 `PointHistory`만 기록하도록 수정했다.

#### 2.2. '내가 올린 자료' 목록 빈 배열 반환

* **문제 상황:** 자료 업로드 후 `GET /api/v1/me/materials` API가 정상(200 OK) 응답에도 불구하고 `{ materials: [], ... }` 빈 목록을 반환했다.
* **원인 분석:** `MaterialRepository`의 JPA 메서드 네이밍(`findByUserIdAndIsDeletedFalse`)이 중첩된 객체 경로(`m.user.id`)를 올바르게 해석하지 못했다.
* **최종 해결:** `MaterialRepository`의 해당 메서드에 `@Query` 어노테이션을 사용하여 `"SELECT m FROM Material m WHERE m.user.id = :userId AND m.isDeleted = FALSE"` JPQL 쿼리를 명시적으로 작성했다.

#### 2.3. 삭제된 자료가 API 응답에 포함됨

* **문제 상황:** 자료 논리적 삭제(`isDeleted = true`) 후에도, 상세 조회(`GET /.../{materialId}`) 및 구매 목록(`GET /.../me/downloads`) API에서 삭제된 자료가 계속 조회되었다.
* **원인 분석:** 조회 로직(Service)에서 `isDeleted` 플래그를 체크하지 않았다.
* **최종 해결:**
    1.  `MaterialService.getMaterial()`: `material.getIsDeleted()`를 체크하여 `true`일 경우 `NotFoundException`을 발생시켰다.
    2.  `MaterialService.getMyDownloadedMaterials()`: DTO 변환 스트림에 `.filter(material -> !material.getIsDeleted())`를 추가하여 삭제된 자료를 필터링했다.

#### 2.4. 리뷰 개수 2배 집계 오류

* **문제 상황:** 리뷰를 1개 작성하면 API 응답에 `reviewCount: 2`로 표시되었다.
* **원인 분석:** `Review` 엔티티 내 불필요한 `reviewCount` 필드가 존재했으며, `ReviewService`에서 엔티티 생성 시점과 별도 메서드(`increaseReviewCount()`) 호출을 통해 카운트를 중복 증가시켰다.
* **최종 해결:**
    1.  `Review` 엔티티에서 `reviewCount` 필드 및 관련 로직을 모두 제거했다.
    2.  `ReviewService`에서 `reviewCount` 관련 로직을 모두 삭제했다.
    3.  `MaterialGetResponseDto`에서 `material.getReviews().size()`를 사용해 동적으로 리뷰 개수를 계산하도록 수정했다.

---

### 3. 인증 및 외부 연동 오류

#### 3.1. OAuth2 로그인 `redirect_uri_mismatch`

* **문제 상황:** 프론트엔드에서 Google 로그인 시 `400 오류: redirect_uri_mismatch`가 발생했다.
* **원인 분석:** 프론트엔드가 백엔드의 인증 엔드포인트(`/oauth2/authorization/google`)를 호출하지 않고, Google 인증 서버로 직접 요청을 보내면서 `redirect_uri`를 프론트엔드 주소(`...:5173`)로 잘못 설정했다.
* **최종 해결:**
    1.  Google Cloud Console의 '승인된 리디렉션 URI'에 **백엔드 엔드포인트**(`.../login/oauth2/code/google`)를 로컬 및 배포용으로 등록했다.
    2.  프론트엔드에 로그인 버튼 클릭 시, 백엔드가 제공하는 인증 주소로 `window.location.href`를 변경하도록 요청했다.
