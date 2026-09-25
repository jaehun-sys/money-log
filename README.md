# 💰 money-log

> **금융 데이터를 정확하게 기록하고, 이를 기반으로 금융 연산과 분석을 확장하기 위한 개인 금융 관리 시스템**

money-log는 금융 데이터를 정규화하고 거래·자산 데이터를 기반으로 금융 연산 및 분석 기능을 구축하기 위한 프로젝트입니다.

DDD + Hexagonal Architecture를 기반으로 Gradle 멀티 모듈 구조를 적용하여 비즈니스 로직과 외부 기술의 의존성 경계를 분리합니다.

---

## 🏗️ 아키텍처

money-log는 Gradle 멀티 모듈을 통해 Core의 비즈니스 로직과 외부 기술의 의존성 경계를 물리적으로 분리합니다.

```text
                     ┌──────────────────────┐
                     │       :api           │
                     │   Web Inbound Adapter │
                     │   Spring MVC / REST  │
                     └──────────┬───────────┘
                                │
                                ▼
                     ┌──────────────────────┐
                     │       :core          │
                     │  Domain / Application│
                     │    Pure Kotlin       │
                     └──────────┬───────────┘
                                │
                                ▼
                     ┌──────────────────────┐
                     │      :storage        │
                     │ Persistence Adapter  │
                     │   JPA / Database     │
                     └──────────────────────┘
```

### `:core`

**비즈니스 코어**

* Domain Model
* Application Service
* Inbound / Outbound Port
* Command / Query
* 순수 Kotlin 기반 구성
* Spring Boot, JPA 등 외부 프레임워크에 직접 의존하지 않음

### `:storage`

**영속성 어댑터**

* Spring Data JPA
* Persistence Adapter
* Repository
* Entity Mapping
* 데이터베이스 접근
* Persistence Audit 정보 관리

### `:api`

**웹 인바운드 어댑터**

* Spring Boot / Spring MVC
* REST API
* Request / Response DTO
* API Validation
* Core Application Service의 Spring Bean 조립

---

## 🛠️ 기술 스택

* **Language:** Kotlin 1.9.22
* **JDK:** Eclipse Temurin Java 21 (LTS)
* **Build:** Gradle Kotlin DSL
* **Web:** Spring Boot / Spring MVC
* **Persistence:** Spring Data JPA
* **Database:** H2 (Test) / PostgreSQL
* **Test:** JUnit 5 / Spring Boot Test / MockK

---

## 🔄 Current Application Flow

외부 요청은 Inbound Port를 통해 Core로 진입하고, 데이터 접근은 Outbound Port를 통해 Persistence Adapter로 위임됩니다.

### 거래 등록

```text
POST /api/v1/transactions
        ↓
TransactionController
        ↓
RecordTransactionUseCase
        ↓
RecordTransactionService
        ↓
SaveTransactionPort
        ↓
TransactionPersistenceAdapter
        ↓
TransactionJpaRepository
```

### 거래 기간 조회

```text
GET /api/v1/transactions
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
TransactionJpaRepository
```

---

## 📌 Core Domain

현재 Core Domain에서는 거래 데이터를 다음과 같이 모델링합니다.

```text
Transaction
├── id
├── userId
├── type
├── amount
├── category
├── memo
└── occurredAt
```

`occurredAt`은 실제 거래가 발생한 시각을 의미하는 비즈니스 데이터입니다.

시스템에서 데이터가 생성된 시점인 `createdAt`과 같은 Audit 정보는 Persistence 계층에서 관리합니다.

세부적인 도메인 및 설계 원칙은 [`docs/PRODUCT.md`](docs/PRODUCT.md)에서 관리합니다.

---

## 🚀 Quick Start

### 1. Clone

```bash
git clone https://github.com/jaehun-sys/money-log.git
cd money-log
```

### 2. Test

```bash
./gradlew test
```

### 3. Run API

```bash
./gradlew :api:bootRun
```

API 서버 실행 후 현재 구현된 거래 API를 확인할 수 있습니다.

```text
GET  /api/v1/transactions
POST /api/v1/transactions
```

---

## 🗺️ Roadmap

### Current

* [x] Core Domain / Money / Transaction
* [x] Hexagonal Architecture 기반 Application Layer
* [x] JPA Persistence Adapter
* [x] 거래 저장 / 조회
* [x] 거래 등록 API
* [x] 기간별 거래 조회 API
* [x] Web Adapter Test
* [x] API Integration Test

### Next

* [ ] 거래 수정 / 삭제
* [ ] 인증 및 사용자 컨텍스트 연계
* [ ] 금융 데이터 정규화
* [ ] 자산 및 금융 분석 기능

상세한 개발 방향과 설계 원칙은 [`docs/PRODUCT.md`](docs/PRODUCT.md)를 참고하세요.

---

## 📚 Documentation

money-log의 프로젝트 방향과 설계 관련 내용은 목적에 따라 별도 문서로 관리합니다.

* **[PRODUCT.md](docs/PRODUCT.md)**
  프로젝트의 목적, 제품 방향, 도메인 모델링 원칙, 아키텍처 원칙, 개발 방법론 및 로드맵

* **[Architecture Decision Records](docs/adr/)**
  주요 아키텍처 및 도메인 설계 결정과 그 배경
