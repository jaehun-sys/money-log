# money-log

> 파편화된 개인 금융 데이터를 표준화하고, 의미 있는 자산 지표로 변환하는 오픈소스 금융 계산 엔진

[![Status](https://img.shields.io/badge/status-active%20development-orange)](#현재-상태)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue)](#라이선스)

## 목차

* [프로젝트 소개](#프로젝트-소개)
* [주요 기능](#주요-기능)
* [빠른 시작](#빠른-시작)
* [아키텍처](#아키텍처)
* [개발 방법](#개발-방법)
* [테스트 전략](#테스트-전략)
* [성능 검증](#성능-검증)
* [CI/CD](#cicd)
* [현재 상태](#현재-상태)
* [로드맵](#로드맵)
* [기여 안내](#기여-안내)
* [설계 원칙](#설계-원칙)
* [프로젝트의 목표](#프로젝트의-목표)
* [프로젝트의 가치](#프로젝트의-가치)
* [라이선스](#라이선스)

## 프로젝트 소개

money-log는 단순한 가계부 웹 애플리케이션이 아닙니다.

은행, 증권사 등 서로 다른 금융기관에서 수집한 개인 금융 데이터를 하나의 일관된 도메인 모델로 해석하고, 이를 기반으로 자산의 현재 상태와 변화 흐름을 계산·분석할 수 있는 재사용 가능한 금융 엔진을 만드는 프로젝트입니다.

money-log가 지향하는 데이터 흐름은 다음과 같습니다.

```text
Raw Financial Data
        ↓
Standardized Domain Data
        ↓
Financial Calculation
        ↓
Asset Information
```

사용자가 여러 금융기관에서 발생한 거래와 자산 데이터를 가져오면 money-log는 이를 단순히 저장하는 데 그치지 않습니다. 금융 도메인의 의미를 해석하여 다음과 같은 정보로 변환합니다.

* 수입과 지출
* 현재 순자산
* 자산 종류별 구성
* 기간별 자산 변화
* 자산 종류별 비중
* 카테고리별 지출
* 투자 자산 현황

즉, money-log의 핵심은 CRUD 서비스가 아니라 금융 계산과 분석입니다.

## 주요 기능

### 금융 데이터 표준화

금융기관마다 데이터 형식이 다르더라도 money-log 내부에서는 동일한 도메인 모델로 해석할 수 있도록 표준화합니다.

```text
[Bank A]
   │
[Bank B] ──┐
           ├──> Normalization ──> Domain Model
[Broker C] ┘                         │
                                     ▼
                               Financial Engine
                                     │
                     ┌────────────────┼────────────────┐
                     ▼                ▼                ▼
                  Assets           Expenses        Analytics
```

### Money Value Object

금액과 통화를 하나의 도메인 객체로 표현합니다.

```text
Money
 ├── amount
 └── currency
```

주요 책임:

* 금액 표현
* 통화 표현
* 금액 연산
* 통화 일치 검증
* 금융 계산의 기본 단위 제공

예를 들어 서로 다른 통화의 금액을 잘못 더하지 않도록 도메인 내부에서 검증합니다.

```text
Money(10_000, KRW)
+
Money(20, USD)

=> Invalid
```

금융 시스템에서는 계산이 실행되는 것보다 잘못된 계산이 발생하지 않는 것이 중요합니다.

### Transaction

사용자의 금융 거래를 표현하는 도메인 객체입니다.

```text
수입
지출
거래 유형
카테고리
금액
통화
메모
발생 시각
사용자
```

Transaction은 단순한 데이터베이스 행이 아니라 금융 활동의 의미와 규칙을 포함하는 도메인 객체로 취급합니다.

거래 발생 시각과 시스템 데이터 생성 시각은 서로 다른 의미로 관리합니다.

* `occurredAt`: 실제 거래가 발생한 시간
* `createdAt`: 시스템에서 거래 데이터가 생성된 시간

`occurredAt`은 Core Domain의 비즈니스 데이터이며, `createdAt`을 포함한 Audit 정보는 Persistence 계층에서 관리합니다.

### Asset

사용자가 보유한 자산을 표현합니다.

```text
현금
주식
부동산
기타 투자 자산
```

향후 자산의 현재 가치와 구성 비율을 계산하고 시각화 데이터로 제공하는 기반이 됩니다.

### 금융 분석

거래 목록을 조회하는 것을 넘어 기간별 금융 흐름을 계산합니다.

* 월별 지출
* 카테고리별 지출
* 누적 자산
* 기간별 자산 변화
* 자산 종류별 비중
* 투자 자산 분석

목표는 사용자가 금융 데이터를 단순히 조회하는 것이 아니라 해석할 수 있도록 만드는 것입니다.

## 빠른 시작

### 요구 사항

* JDK
* Gradle Wrapper
* Git

프로젝트는 Gradle Wrapper를 사용하므로 별도의 Gradle 설치 없이 빌드할 수 있습니다.

### 저장소 클론

```bash
git clone <repository-url>
cd money-log
```

### 빌드

```bash
./gradlew build
```

### 테스트 실행

```bash
./gradlew test
```

Windows 환경에서는 다음 명령을 사용할 수 있습니다.

```bash
gradlew.bat build
gradlew.bat test
```

현재 프로젝트는 Core Domain, Application, Persistence, Web/API 계층까지 구현되어 있습니다.

## 아키텍처

money-log는 Hexagonal Architecture(Ports & Adapters)를 중심으로 설계합니다.

핵심은 기술이 아니라 Core Domain이 중심이 되는 것입니다.

```text
                         External World
                              │
                              ▼
                     ┌───────────────────┐
                     │ Web / HTTP / API  │
                     └─────────┬─────────┘
                               │
                               ▼
                        [ Inbound Port ]
                               │
                               ▼
         ┌──────────────────────────────────────────┐
         │                  :core                   │
         │                                          │
         │  Domain                                  │
         │  ├─ Money                                │
         │  ├─ Transaction                           │
         │  └─ Asset                                │
         │                                          │
         │  Application                             │
         │  ├─ UseCase                              │
         │  └─ Application Service                  │
         │                                          │
         │  Outbound Port                           │
         └───────────────────┬──────────────────────┘
                             │
                             ▼
                       [ Outbound Port ]
                             │
                             ▼
                    ┌──────────────────┐
                    │    :storage      │
                    │ Persistence      │
                    │ Adapter          │
                    │ JPA / Repository  │
                    └────────┬─────────┘
                             │
                             ▼
                       PostgreSQL / DB
```

### 의존성 방향

```text
Web
 │
 ▼
Core
 │
 ▼
Port
 ▲
 │
Storage Adapter
```

실제 의존성의 주도권은 `:core`가 가집니다.

`:core`가 데이터베이스를 직접 호출하는 대신 필요한 행위를 Port로 정의합니다.

```kotlin
interface SaveTransactionPort {
    fun save(transaction: Transaction): Transaction
}
```

`:storage`는 이 Port를 구현합니다. 따라서 데이터베이스 기술이 변경되더라도 Core의 금융 계산 로직을 최대한 보존할 수 있습니다.

### 멀티 모듈 구조

```text
money-log
│
├── build.gradle.kts
├── settings.gradle.kts
│
├── :core
│   └── Domain / Application / Port
│
├── :storage
│   └── JPA / Repository / Persistence Adapter
│
└── :api
    └── HTTP / Controller / Web Adapter
```

#### `:core`

담당:

* Domain
* Value Object
* Entity
* UseCase
* Application Service
* Port

포함하지 않는 것:

* JPA Entity
* 데이터베이스 구현
* Spring Framework 의존성
* HTTP 처리

Application Service 역시 Spring Bean annotation에 의존하지 않는 순수 Kotlin으로 유지합니다. 외부 프레임워크와의 객체 조립은 Composition Root에서 담당합니다.

#### `:storage`

담당:

* Persistence Adapter
* JPA
* Repository
* Entity Mapping
* 데이터베이스 접근
* Persistence Audit 정보 관리

#### `:api`

담당:

* HTTP 요청 처리
* Controller
* Request/Response 변환
* Inbound UseCase 호출
* Core Application Service의 Spring Bean 조립

비즈니스 규칙은 Controller에 넣지 않습니다.

### 거래 기록 처리 흐름

```text
HTTP Request
     │
     ▼
TransactionController
     │
     ▼
RecordTransactionUseCase
     │
     ▼
RecordTransactionService
     │
     ├──> Money 생성
     │
     ├──> Transaction 생성
     │
     ▼
SaveTransactionPort
     │
     ▼
TransactionPersistenceAdapter
     │
     ▼
TransactionJpaRepository
     │
     ▼
Database
```

### 거래 기간 조회 흐름

```text
HTTP Request
     │
     ▼
TransactionController
     │
     ▼
LoadTransactionsUseCase
     │
     ▼
LoadTransactionsService
     │
     ▼
LoadTransactionsPort
     │
     ▼
TransactionPersistenceAdapter
     │
     ▼
TransactionJpaRepository
     │
     ▼
Database
```

거래 기간 조회는 `userId`, `startDate`, `endDate`를 조건으로 사용합니다.

조회 기간은 `startDate <= endDate` 조건을 만족해야 합니다.

API는 사용자의 조회 의도를 날짜 단위로 표현하기 위해 `LocalDate`를 사용합니다.

Core Domain의 `Transaction.occurredAt`은 실제 거래 발생 시각을 표현하므로 `LocalDateTime`을 사용합니다.

Persistence 계층에서는 날짜 단위 조회 조건을 DB의 시간 범위로 변환합니다.

```text
startDate = 2026-09-01
endDate   = 2026-09-30

start = 2026-09-01T00:00:00
end   = 2026-10-01T00:00:00

occurredAt >= start
AND
occurredAt < end
```

종료일의 다음 날 00시를 배타적 상한으로 사용하는 Half-Open Interval 방식으로 날짜 경계를 처리합니다.

### Command와 Query

Inbound Port의 입력 모델은 요청 목적에 따라 Command와 Query로 구분합니다.

```text
상태 변경
    ↓
Command
    ↓
UseCase

상태 조회
    ↓
Query
    ↓
UseCase
```

거래 등록:

```text
HTTP Request
    ↓
RecordTransactionRequest
    ↓
RecordTransactionCommand
    ↓
RecordTransactionUseCase
    ↓
RecordTransactionService
```

거래 조회:

```text
HTTP Request
    ↓
LoadTransactionsQuery
    ↓
LoadTransactionsUseCase
    ↓
LoadTransactionsService
```

HTTP Request DTO와 Core Application 입력 모델은 분리하여 외부 프로토콜과 Core의 결합을 방지합니다.

### Transaction 시간 및 Audit

Transaction의 비즈니스 시간과 시스템 Audit 정보는 분리합니다.

```text
Core Domain

Transaction
├── id
├── userId
├── type
├── amount
├── category
├── memo
└── occurredAt
```

```text
Persistence

TransactionJpaEntity
├── business data
│   ├── id
│   ├── userId
│   ├── type
│   ├── amount
│   ├── category
│   ├── memo
│   └── occurredAt
│
└── audit data
    ├── createdAt
    ├── createdBy
    └── createdPgmId
```

`occurredAt`은 실제 거래가 발생한 시각을 의미하는 **Core Domain의 비즈니스 데이터**입니다.

Persistence 계층에서는 이 값을 `TransactionJpaEntity.occurredAt`으로 저장하지만, 이는 Domain의 비즈니스 데이터를 영속화하기 위한 필드이며 Audit 정보에 해당하지 않습니다.

반면 `createdAt`, `createdBy`, `createdPgmId`는 시스템에서 해당 데이터를 생성한 시점과 행위자, 프로그램을 기록하는 **Persistence Audit 정보**입니다.

따라서 `occurredAt`과 `createdAt`은 서로 다른 의미를 가지며 동일한 시간 정보로 취급하지 않습니다.

* `occurredAt`: 실제 거래가 발생한 시간
* `createdAt`: 시스템에서 거래 데이터가 생성된 시간
* `createdBy`: 시스템에서 거래 데이터를 생성한 행위자
* `createdPgmId`: 거래 데이터를 생성한 프로그램

`userId`는 거래의 비즈니스 소유자를 의미하고, `createdBy`는 시스템에서 데이터를 생성한 행위자를 의미합니다.

현재 인증 기능이 구현되지 않았기 때문에 Persistence 계층에서는 `createdBy = SYSTEM_UNAUTH`를 사용합니다.

`createdPgmId`는 Persistence 경계에서 `MONEY_LOG_API`로 주입합니다.

## 개발 방법

money-log는 `DB → API → 비즈니스 로직` 순서의 전통적인 CRUD 개발 방식과 다르게 개발합니다.

```text
1. Domain Analysis
       │
       ▼
2. Domain Model
       │
       ▼
3. Inbound Port / UseCase
       │
       ▼
4. Outbound Port
       │
       ▼
5. Application Service
       │
       ▼
6. Unit Test
       │
       ▼
7. Persistence Adapter
       │
       ▼
8. Persistence Test
       │
       ▼
9. Web Adapter
       │
       ▼
10. Integration / Performance Test
       │
       ▼
11. CI/CD
       │
       ▼
12. Open Source Release
```

### 1. 도메인 정의

새로운 기능을 개발하기 전에 다음 질문에 답합니다.

```text
무엇을 표현하는가?
어떤 상태를 가지는가?
어떤 행위를 하는가?
어떤 불변식이 존재하는가?
```

### 2. UseCase 정의

외부에서 수행할 수 있는 하나의 비즈니스 행위를 Inbound Port로 표현합니다.

```text
RecordTransactionUseCase
LoadTransactionsUseCase
```

### 3. 외부 의존성 추상화

데이터베이스나 외부 시스템이 필요하면 Core가 필요한 행위를 Port로 정의합니다.

```text
SaveTransactionPort
LoadTransactionsPort
```

### 4. Application Service 구현

Application Service는 Domain과 Port를 조합하여 비즈니스 흐름을 오케스트레이션합니다.

가능한 한 Spring에 의존하지 않는 순수 Kotlin으로 비즈니스 흐름을 유지합니다.

### 5. 테스트로 설계 검증

계층별 책임을 분리하여 테스트합니다.

```text
Domain Test
    ↓
Application Service Test
    ↓
Persistence Slice Test
    ↓
Web Adapter Test
    ↓
Integration Test
    ↓
Performance Test
```

## 테스트 전략

테스트는 하나의 테스트에 모든 책임을 몰아넣지 않고, 실패 원인을 식별할 수 있도록 계층별 책임을 분리합니다.

### Domain Test

Spring과 데이터베이스 없이 금융 규칙을 검증합니다.

```text
Money(KRW) + Money(KRW)
        => Success

Money(KRW) + Money(USD)
        => Invalid

금액 <= 0
        => Invalid
```

### Application Service Test

UseCase가 올바른 비즈니스 흐름을 수행하는지 검증합니다.

```text
Command
  ↓
Validation
  ↓
Money 생성
  ↓
Transaction 생성
  ↓
SaveTransactionPort 호출
```

### Persistence Slice Test

JPA와 데이터베이스 매핑 및 Persistence Adapter의 DB 접근을 검증합니다.

현재 기록된 구현에서는 H2와 `@DataJpaTest` 기반 검증을 사용합니다.

거래 조회에서는 `userId`와 날짜 범위 조건이 실제 DB 조회로 올바르게 변환되는지 검증합니다.

### Web Adapter Test

Spring MVC 기반 Web Adapter의 HTTP 요청/응답, Validation, Binding, UseCase 호출을 검증합니다.

`TransactionControllerTest`에서는 UseCase를 Mock으로 대체하여 Controller 자체의 책임을 검증합니다.

### Integration Test

실제 Spring Context에서 Web Adapter, Core Application Service, Persistence Adapter, JPA, H2가 함께 동작하는 흐름을 검증합니다.

현재 `TransactionControllerIntegrationTest`에서 거래 조회 API를 실제 계층을 통해 검증합니다.

```text
MockMvc
   ↓
TransactionController
   ↓
LoadTransactionsUseCase
   ↓
LoadTransactionsService
   ↓
LoadTransactionsPort
   ↓
TransactionPersistenceAdapter
   ↓
JPA Repository
   ↓
H2
```

통합 테스트에서는 실제 UseCase와 Persistence 계층을 사용하며 Mock을 사용하지 않습니다.

각 테스트는 `@Transactional`을 통해 테스트 종료 후 트랜잭션을 rollback하여 테스트 데이터를 격리합니다.

### 테스트 실행

```bash
./gradlew test
```

## 성능 검증

money-log는 대규모 금융 데이터 처리 성능을 추측하지 않고 측정하는 것을 목표로 합니다.

```text
100만 건 금융 데이터 시뮬레이션
        ↓
처리 시간 측정
        ↓
CPU / Memory / DB 병목 측정
        ↓
필요한 부분만 최적화
        ↓
재측정
```

다음과 같은 가정을 사전에 최적화 근거로 사용하지 않습니다.

```text
"아마 느릴 것이다"
        X
```

측정 결과를 기반으로 병목을 판단합니다.

```text
"측정 결과 이 구간이 병목이었다"
        O
```

향후 검토 대상:

* Batch 처리 전략
* 데이터베이스 쿼리 성능
* Index
* Transaction 경계
* Connection Pool
* Cache
* Redis
* 대용량 데이터 처리 방식
* 병렬 처리

필요성이 측정으로 증명되기 전까지 기술을 미리 도입하지 않습니다.

## CI/CD

최종 목표는 개발자의 로컬 환경에 의존하지 않고 변경사항을 자동으로 검증하는 것입니다.

```text
Developer
    │
    │ git push / Pull Request
    ▼
GitHub
    │
    ▼
GitHub Actions
    │
    ├── Build
    ├── Unit Test
    ├── Persistence Test
    ├── Integration Test
    └── Quality / Coverage Check
             │
             ▼
           Release
```

Gradle Wrapper를 사용하여 개발자 PC와 CI 환경에서 동일한 Gradle 실행 환경을 재현합니다.

기본 검증 명령:

```bash
./gradlew build
./gradlew test
```

## 현재 상태

🚧 Active Development

현재 프로젝트는 다음 단계로 진행되고 있습니다.

```text
[완료]
Core Domain
    │
    ▼
[완료]
Application / Port
    │
    ▼
[완료]
Persistence Adapter
    │
    ▼
[완료]
Web Adapter
    │
    ▼
[완료]
Integration Test
    │
    ▼
[다음 단계]
Financial Analytics
    │
    ▼
Performance
    │
    ▼
CI/CD
    │
    ▼
Open Source Release
```

현재 확인된 핵심 구현:

* **:core의 순수 Kotlin Domain/Application 구조**

  * Money Value Object
  * Transaction Domain
  * Inbound / Outbound Port
  * Application Service
  * Command / Query 모델
* **:storage Persistence Adapter**

  * JPA 기반 영속성 구조
  * 거래 저장 및 조회
  * H2 기반 Persistence Slice Test
  * Persistence 경계에서 Audit 정보 관리
  * 날짜 단위 조회 조건을 Half-Open Interval 시간 범위로 변환
* **:api Web Adapter**

  * Spring MVC 기반 REST API
  * 거래 등록 API
  * 거래 기간 조회 API
  * API Validation (Bean Validation)
  * WebMvcTest 기반 Controller 테스트
  * 실제 Spring Context 기반 Integration Test

### 로드맵

#### Phase 0 — Data Ingestion (데이터 수집 파이프라인)

* [ ] 수동 입력 및 CSV 대량 임포트 (Bulk Upload) 기능 설계
* [ ] Android 기반 푸시 알림(Notification) 인터셉터 PoC (프론트엔드 연동)
* [ ] 결제/송금 알림 정규식(Regex) 파싱 엔진 (토스, 카뱅, 주요 카드사)
* [ ] 이메일(IMAP) 기반 결제 내역 자동 파싱 봇 설계 (iOS 유저 및 대안용)

#### Phase 1 — Domain Foundation

* [x] Money
* [x] Transaction 기본 모델
* [ ] Asset 모델 확장
* [ ] 금융 계산 규칙 확장
* [ ] Domain Test 확대

#### Phase 2 — Application Layer

* [x] Inbound Port
* [x] Outbound Port
* [x] Application Service
* [x] 거래 등록 UseCase
* [x] 거래 조회 UseCase
* [ ] UseCase 확장
* [ ] In-Memory Fake Repository 기반 테스트 확대

#### Phase 3 — Persistence

* [x] Persistence Adapter
* [x] JPA Mapping
* [x] Persistence Slice Test
* [x] 거래 조회용 Persistence Port 정의
* [x] 거래 조회 DB 구현
* [ ] PostgreSQL 기반 실제 환경 검증

#### Phase 4 — Web/API

* [x] Web Adapter
* [x] REST API
* [x] API Validation
* [x] 거래 등록 API
* [x] 거래 기간 조회 API
* [x] Controller Test
* [x] API Integration Test
* [x] 테스트 트랜잭션을 통한 데이터 격리

#### Phase 5 — Financial Analytics

* [ ] 기간별 지출 분석
* [ ] 누적 자산 계산
* [ ] 자산 비중 계산
* [ ] 투자 자산 분석
* [ ] 시각화용 데이터 모델

#### Phase 6 — Performance

* [ ] 100만 건 데이터 생성
* [ ] Benchmark
* [ ] DB Query Profiling
* [ ] Transaction Profiling
* [ ] 병목 구간 최적화
* [ ] 최적화 전후 수치 기록

#### Phase 7 — Engineering Automation

* [ ] GitHub Actions
* [ ] Automated Test
* [ ] Coverage
* [ ] Build Verification
* [ ] Release Automation

#### Phase 8 — Open Source

* [ ] README 완성
* [ ] Architecture Documentation
* [ ] ADR 정리
* [ ] Contribution Guide
* [ ] Example Application
* [ ] Library 형태 배포 검토

## 기여 안내

money-log는 금융 도메인 모델링, 아키텍처, 테스트, 데이터 처리, 성능 검증, 자동화에 관심 있는 개발자의 기여를 환영합니다.

### 기여 전 확인 사항

변경을 시작하기 전에 다음을 먼저 확인해 주세요.

1. 변경하려는 기능이 어떤 금융 도메인 개념을 표현하는지 정의합니다.
2. 해당 개념의 상태, 행위, 불변식을 정리합니다.
3. 기존 Domain Model과 Port 구조를 확인합니다.
4. 필요한 경우 ADR 또는 설계 문서를 업데이트합니다.
5. 변경 범위에 맞는 테스트를 추가합니다.

### 권장 작업 순서

```text
Issue 또는 변경 목적 확인
        ↓
Domain Model 검토
        ↓
UseCase / Port 설계
        ↓
Application Service 구현
        ↓
Domain / Application Test 작성
        ↓
Persistence Adapter 구현
        ↓
Persistence / Integration Test 작성
        ↓
문서 업데이트
        ↓
Pull Request 생성
```

### Pull Request 체크리스트

* [ ] 변경 목적과 범위가 명확한가?
* [ ] 금융 도메인 규칙이 Domain에 표현되어 있는가?
* [ ] Controller에 비즈니스 규칙을 넣지 않았는가?
* [ ] Core가 Spring, JPA, DB에 직접 의존하지 않는가?
* [ ] 필요한 Port를 Core가 소유하고 있는가?
* [ ] 단위 테스트와 계층별 테스트를 추가했는가?
* [ ] `./gradlew build`가 통과하는가?
* [ ] 관련 문서와 로드맵을 업데이트했는가?

### 설계 변경 기록

단순히 무엇을 선택했는지만 기록하지 않고 다음 내용을 함께 남깁니다.

```text
왜 선택했는가?
무엇을 포기했는가?
어떤 조건에서 다시 변경할 것인가?
```

## 설계 원칙

### Core는 순수하게 유지합니다

`:core`는 Spring, JPA, 데이터베이스 기술에 직접 의존하지 않습니다.

Application Service 역시 Spring Bean annotation에 의존하지 않습니다. Core Application Service의 객체 조립은 외부 Composition Root에서 담당합니다.

### 데이터베이스보다 도메인을 먼저 설계합니다

스키마보다 Domain Model과 Business Rule을 먼저 정의합니다.

### Port는 Core가 소유합니다

Core가 외부 시스템에 요구하는 행위를 Interface로 정의합니다.

### Adapter는 교체 가능해야 합니다

데이터베이스, Web, Cache 등의 기술이 Core를 오염시키지 않도록 합니다.

### 입력 검증은 경계에서 수행합니다

Command 등 입력 경계에서 명백히 잘못된 데이터를 빠르게 차단합니다.

### Command와 Query를 구분합니다

상태 변경 요청과 상태 조회 요청을 서로 다른 입력 모델로 표현합니다.

* 상태 변경 → Command
* 상태 조회 → Query

### Application Service는 오케스트레이터입니다

Application Service가 모든 비즈니스 규칙을 독점하지 않도록 합니다. Domain은 자신의 규칙을 소유하고 Service는 흐름을 조합합니다.

### Transaction 시간과 Audit을 분리합니다

거래의 실제 발생 시각인 `occurredAt`과 시스템 데이터 생성 시각인 `createdAt`을 구분합니다.

`occurredAt`은 Core Domain의 비즈니스 데이터로 관리하고, Persistence 계층에서는 이를 영속화합니다.

`createdAt`, `createdBy`, `createdPgmId`는 Persistence Audit 정보로 관리합니다.

### API 입력 모델과 Core 입력 모델을 분리합니다

외부 HTTP 프로토콜에 사용되는 Request/Response DTO와 Core Application의 Command/Query를 분리합니다.

외부 API의 표현 방식이 변경되더라도 Core Application 모델이 직접 영향을 받지 않도록 합니다.

### 테스트 책임을 분리합니다

각 테스트는 서로 다른 책임과 실패 원인을 검증합니다.

```text
Domain Test
→ Domain 규칙

Application Service Test
→ Application 흐름

Persistence Slice Test
→ JPA / Repository / DB 접근

Web Adapter Test
→ HTTP / Validation / Binding / Controller

Integration Test
→ 실제 계층 간 Wiring 및 전체 API 흐름
```

Integration Test에서는 실제 계층을 연결하여 검증하되, 테스트 데이터는 테스트 트랜잭션 rollback을 통해 격리합니다.

### 정확성을 성능보다 먼저 증명합니다

금액 모델은 초기 단계에서 `BigDecimal` 기반 접근을 사용합니다.

그 이유는 다음과 같습니다.

* 소수점 계산의 명시적인 제어
* 통화별 계산 규칙 확장
* 반올림 정책의 명시화
* 큰 값에 대한 오버플로우 위험 감소

다만 `BigDecimal`을 영구적인 정답으로 고정하지는 않습니다. 향후 100만 건 이상의 금융 데이터 시뮬레이션을 수행하고 실제 병목을 측정한 뒤, 필요하다면 integer scaling 등의 다른 전략을 검토합니다.

핵심 원칙은 다음과 같습니다.

> 추측으로 최적화하지 않고 측정한 뒤 최적화합니다.

## 프로젝트의 목표

money-log가 최종적으로 지향하는 형태는 다음과 같습니다.

```text
              Financial Institutions
             /        |        \
          Bank     Broker     Other
             \        |        /
              \       |       /
               ▼      ▼      ▼
             Raw Financial Data
                    │
                    ▼
               Normalization
                    │
                    ▼
              money-log Core
        ┌────────────┼────────────┐
        ▼            ▼            ▼
      Money      Transaction     Asset
        │            │            │
        └────────────┼────────────┘
                     ▼
             Financial Calculation
                     │
        ┌────────────┼────────────┐
        ▼            ▼            ▼
      Assets       Expenses     Portfolio
        │            │            │
        └────────────┼────────────┘
                     ▼
              Meaningful Data
                     │
           ┌─────────┴─────────┐
           ▼                   ▼
        Dashboard              API
           │                   │
           ▼                   ▼
       Visualization      Reusable Engine
```

최종적으로는 특정 웹 애플리케이션에 종속된 코드가 아니라 다음을 목표로 합니다.

> 어떤 금융 데이터 입력 시스템과도 연결할 수 있고, 어떤 저장소나 UI에서도 사용할 수 있는 금융 계산 Core

## 프로젝트의 가치

money-log의 가치는 단순히 가계부를 만드는 데 있지 않습니다.

이 프로젝트는 다음을 증명하는 것을 목표로 합니다.

1. 금융 도메인을 객체와 규칙으로 모델링할 수 있는가?
2. 프레임워크와 핵심 비즈니스 로직을 분리할 수 있는가?
3. 외부 인프라를 Adapter로 격리할 수 있는가?
4. 금융 데이터의 정밀성과 정합성을 보장할 수 있는가?
5. 테스트를 통해 설계의 안정성을 증명할 수 있는가?
6. 대규모 데이터에서 실제 성능을 측정하고 개선할 수 있는가?
7. CI/CD를 통해 반복 가능한 개발 파이프라인을 만들 수 있는가?
8. 다른 개발자가 프로젝트를 이해하고 기여할 수 있는가?

money-log는 다음의 전 과정을 단계적으로 완성해가는 엔지니어링 프로젝트입니다.

```text
도메인 모델링
    ↓
아키텍처
    ↓
테스트
    ↓
데이터 처리
    ↓
성능
    ↓
자동화
    ↓
오픈소스 운영
```

## 라이선스

Apache License 2.0
