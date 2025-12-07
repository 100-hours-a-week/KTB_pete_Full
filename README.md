# 🌿 **Breathe – Backend (Spring Boot REST API)**
### 감정을 편하게 기록하고 공유할 수 있는 멘탈 헬스 기반 커뮤니티 서비스의 백엔드 서버
<br><br>

**Breathe**는 익명 기반 감정 공유 커뮤니티 서비스로,<br>
사용자가 부담 없이 자신의 감정을 기록하고, 공감과 피드백을 주고받을 수 있는 공간을 목표로 합니다.  
<br><br>
이 레포지토리는 해당 서비스의 **Backend(Spring Boot 기반 REST API)** 구현체로,<br>
Frontend(Vanilla JS)와 연동하여 **인증·게시글·댓글·좋아요·파일 업로드** 기능을 제공합니다.

<br><br>

---
🎬 시연 이미지 / 영상<br>
회원가입<br>


https://github.com/user-attachments/assets/27466914-5813-4f22-ad08-a30e109e7a04



<br><br>
로그인 및 메인페이지<br>



https://github.com/user-attachments/assets/2787f27a-f504-463b-99e5-98e6622b6bc6


<br><br>
게시글 관련<br>



https://github.com/user-attachments/assets/6e2cba2a-24cd-4fcc-811b-2df67b6dfc7e


<br><br>
게시글/댓글/회원정보/비밀번호 수정<br>


https://github.com/user-attachments/assets/bf39da6a-b356-4c26-8cb7-1d114d7bf4db



https://github.com/user-attachments/assets/4ebb19ca-8ef1-4660-9f64-b1536fede022


<br><br>
로그아웃<br>



https://github.com/user-attachments/assets/9ca1e343-5e13-4163-a9a9-5d4d7017a7ce



<br><br>
---

## 🚀 **주요 기능 (API 개요)**
<br><br>
### 🔐 **인증 / 사용자**
<br><br>
- 이메일 기반 회원가입 (프로필 이미지 업로드 포함)
- 로그인 (JWT Access Token 발급 + Refresh Token 쿠키 발급)
- Access Token 재발급 (`/auth/refresh`)
- 로그아웃 (DB Refresh Token 폐기 + 쿠키 삭제)
- 내 정보 조회 / 닉네임 & 프로필 이미지 수정
- 비밀번호 변경 (신규 비밀번호 & 확인 값 검증)
- 회원 탈퇴

<br><br>

### 📝 **게시글 (Posts)**
<br><br>
- 게시글 생성 (제목 / 내용 / 이미지 1개 업로드, `multipart/form-data`)
- 게시글 수정 (내용 및 이미지 교체)
- 게시글 삭제
- 게시글 단건 조회
  - 조회수 자동 증가
  - 작성자 정보 포함
  - 로그인 상태일 경우, 현재 사용자의 좋아요 여부(`liked`) 포함
- 게시글 목록 조회
  - 페이지네이션 (page / size)
  - 정렬 기준: 최신순(createdAt), 제목(title), 조회수(views), 좋아요(likes)
  - 정렬 방향: 오름차순 / 내림차순

<br><br>

### 💬 **댓글 (Comments)**
<br><br>
- 특정 게시글에 대한 댓글 생성
- 댓글 수정 / 삭제 (작성자 검증 포함)
- 댓글 목록 조회 (게시글 기준)
  - 페이지네이션
  - 정렬 기준: createdAt
  - 작성자 정보 포함
  - 좋아요 수 포함

<br><br>

### 👍 **좋아요 (Likes)**
<br><br>
- 게시글 좋아요 / 좋아요 취소
- 댓글 좋아요 / 좋아요 취소
- 좋아요 수 집계
  - 게시글 좋아요: `post_likes` 테이블 + Post 엔티티의 likes 컬럼
  - 댓글 좋아요: 집계 쿼리(`IN` + `GROUP BY`)로 여러 댓글의 좋아요 수를 한 번에 조회

<br><br>

---

## 🧩 **기술 스택**
<br><br>
### ⚙️ **Backend**
<br><br>
- Java 21
- Spring Boot 3
- Spring Web (REST API)
- Spring Data JPA
- Spring Security
- Spring Validation (Jakarta Validation)
- MySQL + JDBC
- JWT (jjwt: `io.jsonwebtoken`)
- Swagger / OpenAPI (`springdoc-openapi-starter-webmvc-ui`)

<br><br>

### 🗂 **빌드 & 기타**

- Gradle
- Jacoco (테스트 커버리지 리포트)
- 로컬 파일 스토리지 (`./uploads`)

<br><br>

---

## 🔐 **인증 / 보안 설계**<br>

### ✅ 토큰 구조
<br><br>
- **Access Token (JWT)**
  - `Authorization: Bearer <token>` 헤더로 전달
  - `JwtProviderImpl`에서 HS256 알고리즘으로 서명
  - `userId`를 `subject`로 두고, `email`을 claim으로 포함
  - 만료 시간 내장 (기본 30분, 설정으로 조정 가능)
<br><br>
- **Refresh Token**
  - 랜덤 512bit 토큰 문자열 (Base64 URL Safe)
  - 엔티티: `RefreshToken (user_refresh_tokens)`
  - 유저당 1개 유지 정책
  - HttpOnly 쿠키(`refreshToken`)로 브라우저에 전달  
    (`AuthCookieUtil.addRefreshTokenCookie`)

<br><br>

### 🔒 인증 흐름
<br><br>
1. **로그인**
   - `/auth/login`  
   - 이메일/비밀번호 검증 후:
     - Access Token(헤더로 사용) 발급
     - Refresh Token 발급 후 DB 저장 + HttpOnly 쿠키 설정
<br><br>
2. **인증이 필요한 API 호출**
   - `JwtAuthenticationFilter`가 `Authorization` 헤더의 Bearer 토큰 검증
   - 검증 성공 시 `SecurityContext`에 `userId`를 principal로 저장
   - 컨트롤러에서 `@CurrentUserId Long userId`로 바로 사용
<br><br>
3. **토큰 재발급**
   - `/auth/refresh`  
   - Refresh Token 쿠키 검증 → DB 토큰 유효성/만료 여부 확인  
   - 새 Access Token + 새 Refresh Token 재발급
<br><br>
4. **로그아웃**
   - `/auth/logout`  
   - DB에서 Refresh Token 삭제
   - Refresh Token 쿠키 삭제

<br><br>

### 🧱 권한 제어
<br><br>
- `SecurityConfig`에서 경로별 접근 제어 설정
  - `permitAll`
    - `/auth/**`
    - Swagger 관련 (`/v3/api-docs/**`, `/swagger-ui/**` 등)
    - 정적 업로드 파일(`/uploads/**`)
    - 게시글/댓글 **조회용 GET API** (`GET /board/**`)
  - 그 외 모든 API → **인증 필수**
- 세션 사용 X → `SessionCreationPolicy.STATELESS`
- `formLogin`, `httpBasic` 비활성화

<br><br>

---

## 📦 **공통 모듈 요약**
<br>
### 💡 `ApiResponse<T>`
<br><br>
- 모든 API 응답을 단일 포맷으로 통일
<br><br>

{
  "isSuccess": true,
  "code": 200,
  "message": "OK",
  "result": { ... }
}<br><br>
헬퍼 메서드
<br>
ApiResponse.ok(T result)
<br>
ApiResponse.ok(String message, T result)
<br>
ApiResponse.fail(int httpStatus, String message)

<br><br>
⚠️ ErrorCode & BusinessException<br>
ErrorCode enum으로 에러 코드/메시지/HTTP 상태 관리
<br><br>
예: BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, MEMBER_NOT_FOUND, SERVER_ERROR 등
<br><br>
비즈니스 로직에서 throw new BusinessException(ErrorCode.XYZ) 형태로 사용

<br><br>
🧯 전역 예외 핸들러<br>
ValidationExceptionHandler
<br><br>
@Valid 검증 실패 → 첫 번째 필드 에러를 메시지로 가공하여 400 응답
<br><br>
BusinessExceptionHandler
<br><br>
BusinessException 처리, ErrorCode 기준으로 상태 코드/메시지 반환
<br><br>
FallbackExceptionHandler
<br><br>
처리되지 않은 모든 예외를 500으로 응답
<br><br>
로그에 전체 스택트레이스 기록

<br><br>
🧰 유틸 & 공통 기능<br>
Numbers, Strings, Times
<br><br>
null-safe 변환, 기본값 처리, ISO-8601 시간 포맷
<br><br>
PostMapper, CommentMapper, UserMapper
<br><br>
엔티티 → 응답 DTO 매핑
<br><br>
AuthCookieUtil
<br><br>
Refresh Token 쿠키 설정/삭제
<br><br>
CurrentUserIdArgumentResolver
<br><br>
@CurrentUserId Long userId 파라미터에 현재 로그인 사용자 ID 주입

<br><br>

🗄️ 도메인 구조<br>
👤 User<br>
필드: id, email, password, nickname, profileImageUrl, createdAt, updatedAt
<br><br>
기능:
<br><br>
회원가입 (User.create)
<br><br>
프로필 수정 (updateProfile)
<br><br>
비밀번호 변경 (changePassword)

<br><br>
📝 Post<br>
필드: id, authorId, title, content, image, comments, likes, views, createdAt, updatedAt
<br><br>
기능:
<br><br>
게시글 생성 (Post.create)
<br><br>
일부 수정 (updatePartial)
<br><br>
조회수/댓글수/좋아요수 증가 & 감소
<br><br>
수정 시점 갱신 (touchUpdatedAt)

<br><br>
💬 Comment<br>
필드: id, postId, authorId, content, createdAt, updatedAt
<br><br>
기능:
<br><br>
댓글 생성 (Comment.create)
<br><br>
내용 수정 (updateContent)
<br><br>
수정 시점 갱신 (touchUpdatedAt)

<br><br>
👍 PostLike / CommentLike<br>
게시글/댓글과 유저의 좋아요 관계를 나타내는 엔티티
<br><br>
PostLikeService, CommentLikeService에서 중복 방지 및 카운트 관리

<br><br>
🔁 RefreshToken<br>
필드: id, userId, token, expiresAt, createdAt
<br><br>
기능:
<br><br>
create, updateToken, isExpired

<br><br>

▶️ 실행 방법<br>
1. 레포지토리 클론<br>
git clone https://github.com/100-hours-a-week/KTB_pete_Full.git<br>
cd KTB_pete_Full
<br><br>
2. 환경 변수 설정<br>
application.yml은 아래와 같이 .env 또는 시스템 환경 변수와 연동됩니다.
<br><br>

spring:<br>
  datasource:<br>
    url: ${DB_URL}<br>
    username: ${DB_USERNAME}<br>
    password: ${DB_PASSWORD}<br>
<br><br>
app:<br>
  jwt:<br>
    access-token-secret: ${APP_JWT_ACCESS_TOKEN_SECRET:local-dev-access-secret-change-me}<br>
    refresh-token-secret: ${APP_JWT_REFRESH_TOKEN_SECRET:local-dev-refresh-secret-change-me}<br>
    access-token-expiration-minutes: 30<br>
    refresh-token-expiration-days: 30<br>
<br><br>
DB_URL (예: jdbc:mysql://localhost:3306/community?useSSL=false&serverTimezone=Asia/Seoul)<br><br>

DB_USERNAME<br><br>

DB_PASSWORD<br><br>

APP_JWT_ACCESS_TOKEN_SECRET<br><br>

APP_JWT_REFRESH_TOKEN_SECRET<br><br>

<br><br>
3. DB 준비<br>
MySQL에 스키마(예: community) 생성<br><br>

엔티티와 매핑된 테이블 생성 (DDL은 별도 SQL 또는 JPA 스키마 생성 전략에 맞게 구성)<br><br>

<br><br>
4. 애플리케이션 실행<br>

./gradlew bootRun<br><br>
# 또는<br>
./gradlew build<br>
java -jar build/libs/*.jar<br>
기본 포트: http://localhost:8080<br>

<br><br>
5. Swagger UI 확인<br>
Swagger UI:<br>
http://localhost:8080/swagger-ui/index.html

<br><br>

🧪 테스트<br>
Gradle Jacoco 설정 포함

./gradlew test jacocoTestReport<br>
리포트 위치 예시: build/reports/jacoco/test/html/index.html

<br><br>

👨‍💻 나의 담당 역할 (Backend)<br>
REST API 전반 설계 및 구현
<br><br>
도메인 모델링 및 JPA 엔티티 설계
<br><br>
JWT 기반 인증 구조 설계
<br><br>
Access Token + Refresh Token 이중 구조
<br><br>
HttpOnly 쿠키 기반 Refresh Token 관리
<br><br>
@CurrentUserId ArgumentResolver로 컨트롤러 코드 간결화
<br><br>
전역 예외 처리 및 에러 응답 구조 통일 (ApiResponse, ErrorCode)
<br><br>
게시글/댓글/좋아요 도메인 로직 구현
<br><br>
조회수 및 댓글/좋아요 수 증가/감소 처리
<br><br>
댓글 좋아요 수 N+1 방지용 집계 쿼리 설계
<br><br>
파일 업로드 인프라 구현
<br><br>
LocalFileStorageService로 uploads/ 디렉토리 관리
<br><br>
/uploads/** 정적 리소스 서빙 설정
<br><br>
Swagger/OpenAPI 설정
<br><br>
공통 실패 응답(400/401/403/404/500) 전역 자동 적용

<br><br>
🌱 향후 확장 계획<br>
비밀번호 암호화(Bcrypt 등) 도입
<br><br>
정렬/필터링 조건 확장 (예: 태그, 검색 키워드)
<br><br>
소프트 삭제(탈퇴/삭제 데이터 보존) 도입
<br><br>
알림/멘션 기능 추가
<br><br>
운영 환경을 고려한 S3 등 외부 스토리지 연동

<br><br>

🎬 ERD / 시연 이미지 / 영상<br>
ERD 이미지 삽입 위치
<br><br>

API 시연 GIF / 스크린샷 삽입 위치<br>


<br><br>

📌 관련 레포지토리<br>
Frontend (Vanilla JS)<br>
👉 https://github.com/100-hours-a-week/KTB_pete_Full_Front
<br><br>
Backend (현재 레포)<br>
👉 https://github.com/100-hours-a-week/KTB_pete_Full
