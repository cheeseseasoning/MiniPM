# MiniPM

소규모 개발팀과 학생 팀 프로젝트를 위한 **프로젝트·이슈 관리 웹 서비스**입니다.

프로젝트별 멤버와 권한을 관리하고, 이슈의 담당자·진행 상태·댓글·해결 과정을 기록합니다. 주요 변경 사항은 WebSocket을 통해 관련 사용자에게 실시간으로 전달합니다.

## 프로젝트 정보

| 항목 | 내용 |
|---|---|
| 개발자 | 최희조 |
| 개발 기간 | 2026.09.14 ~ 2026.09.30 |
| 개발 형태 | 개인 프로젝트 |
| 과정명 | MSA 기반 Full Stack 개발자 과정 |

## 주요 기능

- 일반 회원가입 및 카카오 OAuth2 로그인
- 프로젝트 생성과 `ADMIN`·`MEMBER` 권한 관리
- 이메일 기반 프로젝트 멤버 추가
- 이슈 등록·조회·수정·논리 삭제
- 담당자·유형·우선순위·진행 상태 관리
- 상태 변경 이력과 해결 내용 기록
- 댓글 등록·수정·논리 삭제
- WebSocket/STOMP 기반 개인별 실시간 알림
- 개인 및 관리 프로젝트 대시보드

## 주요 업무 규칙

- 프로젝트 생성자의 `ADMIN` 자동 등록
- 프로젝트 참여자만 프로젝트와 이슈에 접근
- 프로젝트 멤버만 이슈 담당자로 지정
- 미완료 담당 이슈가 있는 멤버의 제거 제한
- 이슈 완료 시 해결 내용 필수 입력
- 상태 변경자·변경 시각·이전 상태·변경 상태 기록
- 알림 발생자 본인 제외 및 중복 수신자 제거
- 논리 삭제를 통한 댓글과 상태 이력 보존

## 기술 스택

| 구분 | 기술 |
|---|---|
| Backend | Java 11, Spring Boot 2.7.3, Spring MVC |
| Security | Spring Security, Kakao OAuth2 |
| Database | Oracle, MyBatis |
| Frontend | Thymeleaf, HTML, CSS, JavaScript, Bootstrap |
| Realtime | WebSocket, STOMP, SockJS |
| Build·Tools | Maven, STS, Git, GitHub |

## 시스템 구조

```text
사용자
  ↓
Thymeleaf · JavaScript
  ↓
Spring MVC Controller
  ↓
Service
  ├─ 권한 및 입력값 검증
  ├─ 업무 규칙 처리
  ├─ 트랜잭션 관리
  └─ 알림 대상 계산
  ↓
MyBatis Mapper
  ↓
Oracle Database

Service → NotificationService → WebSocket/STOMP → 사용자별 알림
```

## 주요 데이터 구조

| 테이블 | 역할 |
|---|---|
| `MEMBER` | 회원과 로그인 정보 |
| `PROJECT` | 프로젝트 기본 정보 |
| `PROJECT_MEMBER` | 프로젝트 참여자와 역할 |
| `ISSUE` | 이슈·담당자·상태·해결 정보 |
| `ISSUE_COMMENT` | 이슈별 댓글 |
| `ISSUE_STATUS_HISTORY` | 상태 변경 이력 |
| `NOTIFICATION` | 사용자별 알림과 읽음 상태 |

## 핵심 구현

### 상태 변경과 이력 관리

이슈 상태 수정, 해결 내용, 변경 이력과 알림 생성을 하나의 트랜잭션으로 처리합니다. `SELECT FOR UPDATE`를 적용해 동일 이슈의 동시 변경을 순차적으로 처리하고, 현재 상태와 이력의 일관성을 유지합니다.

### 실시간 알림

알림을 DB에 먼저 저장한 뒤 `/user/queue/notifications` 사용자 전용 채널로 전송합니다. 연결 실패 시 자동 재연결하고, 알림 ID를 기준으로 중복 표시를 방지합니다.

### 논리 삭제

프로젝트·이슈·댓글 삭제 시 데이터를 즉시 제거하지 않고 삭제 정보를 기록합니다. 댓글과 상태 이력을 보존하여 업무 기록과 변경 과정을 추적할 수 있습니다.

## 실행 방법

### 실행 환경

- Java 11
- Oracle Database
- Maven
- Kakao Developers 애플리케이션

### 설정

`src/main/resources/application.properties`에서 Oracle 접속 정보를 확인합니다.

카카오 로그인을 사용하려면 다음 환경변수를 등록합니다.

```text
KAKAO_CLIENT_ID=카카오 REST API 키
KAKAO_CLIENT_SECRET=카카오 Client Secret
```

### 실행

```bash
mvnw.cmd spring-boot:run
```

실행 후 다음 주소로 접속합니다.

```text
http://localhost:8080
```

## 향후 계획

- 프로젝트 일정 및 종료일 관리
- 멤버 초대·승인 기능
- 파일 첨부 기능
- GitHub Issue 연동
- 이슈 상태 단계 확장
- 테스트 및 배포 자동화

## 개발 경험

CRUD에서 시작해 권한, 상태 이력, 댓글, 대시보드와 실시간 알림으로 기능을 확장했습니다. 기능 구현뿐 아니라 데이터 보존, 사용자 권한과 업무 규칙을 함께 설계하는 경험을 목표로 진행한 프로젝트입니다.
