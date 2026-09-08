# 로컬 개발 실행

## 준비

- Java 21: 현재 PC의 설치 경로는 아래 명령을 참고합니다.
- Docker Desktop을 설치하고 Linux containers 엔진이 실행 중이어야 합니다.
- 15432, 16379 포트가 다른 프로그램에서 사용 중이면 먼저 충돌을 해결합니다.

프로젝트 폴더의 PowerShell에서 실행합니다.

```powershell
$env:JAVA_HOME = 'C:\Users\hyochan.DESKTOP-HP429AQ\.jdks\ms-21.0.12.1'
$env:GRADLE_USER_HOME = Join-Path $env:USERPROFILE '.gradle'
docker compose up -d --wait
.\gradlew.bat test
.\gradlew.bat bootRun
```

기본 프로필은 local입니다. 개발용 DB 계정은 Compose와 application-local.properties에 맞춰져 있습니다. DB_URL, DB_USERNAME, DB_PASSWORD, REDIS_HOST, REDIS_PORT로 접속 설정을 변경할 수 있습니다. Compose의 DB 계정을 변경하면 애플리케이션 환경변수도 함께 변경해야 합니다.

로컬 외 환경은 활성 프로필과 DB/Redis 연결 설정을 별도로 지정해야 합니다. 현재 설정은 로컬 개발용입니다.

Flyway가 accounts 테이블을 생성하고 Hibernate는 ddl-auto=validate로 엔티티와 스키마의 일치를 검증합니다. 생성/수정 시각은 JPA Auditing으로 기록합니다.

회원가입 POST /api/auth/signup만 비인증 요청을 허용합니다. 다른 경로는 차단합니다. 이메일 인증, 로그인, JWT는 아직 구현되지 않았습니다. 현재 회원가입은 로컬 개발용입니다.

## 상태 확인 및 종료

```powershell
docker compose ps
docker compose logs postgres redis
docker compose exec postgres psql -U flower -d flower_delivery -c '\d accounts'
docker compose stop
```

accounts 테이블은 애플리케이션 또는 Spring Boot 테스트가 DB에 연결되어 Flyway를 실행한 뒤 생성됩니다. 볼륨에 저장된 DB 데이터는 stop으로 삭제되지 않습니다.


## 회원가입 테스트

POST http://localhost:8080/api/auth/signup
Content-Type: application/json

```json
{
  "email": "customer@example.com",
  "password": "Example-password123!",
  "role": "CUSTOMER",
  "name": "홍길동",
  "phone": "010-1234-5678"
}
```

성공은 201이며 accountId, email, role, status를 반환합니다. CUSTOMER, STORE_OWNER, RIDER를 지원하며 ADMIN은 400입니다. RIDER의 vehicleInfo는 선택 항목이고 승인 상태는 PENDING입니다.

이메일은 소문자로 저장하고 중복 가입은 409로 처리합니다. 입력 오류는 400입니다. 비밀번호는 8자 이상, UTF-8 72바이트 이하이며 BCrypt 해시만 저장합니다. 계정과 역할 프로필은 같은 트랜잭션에서 저장합니다.

## 전용 DB에서 통합 테스트

개발 데이터와 분리하려면 테스트 DB를 최초 한 번 생성한 뒤 DB_URL을 지정합니다.

```powershell
docker compose exec postgres createdb -U flower flower_delivery_test
$env:DB_URL = 'jdbc:postgresql://localhost:15432/flower_delivery_test'
.\gradlew.bat test
Remove-Item Env:DB_URL
```

전용 DB가 이미 있으면 createdb 단계는 생략합니다. 테스트 DB의 스키마도 Flyway가 생성합니다. 일반 테스트는 롤백하고 동시 가입 테스트는 자신이 생성한 데이터만 정리합니다.
