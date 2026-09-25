# 💰 money-log (금융 비즈니스 코어 엔진)

> **"금융 데이터를 정확하게 기록하고 분석하기 위한 도메인 중심의 개인 금융 관리 시스템"**

본 프로젝트는 단순한 가계부를 넘어 금융 데이터를 정규화하고,
거래·자산 데이터를 기반으로 금융 연산 및 분석을 수행하기 위한
DDD + Hexagonal Architecture 기반의 멀티 모듈 프로젝트입니다.

---

## 🏗️ 아키텍처 아키타입 (Multi-Module System)

본 시스템은 Gradle 멀티 모듈을 통해
비즈니스 로직과 외부 기술의 의존성 경계를 물리적으로 분리합니다.

* `:core` - **비즈니스 코어**. 순수 Kotlin으로 구성되며 Spring Boot, JPA 등 외부 프레임워크에 의존하지 않습니다.
* `:storage` - **영속성 어댑터**. Spring Data JPA를 통해 데이터베이스와 Core를 연결합니다.
* `:api` - **웹 인바운드 어댑터**. Spring Boot 기반 REST API를 통해 외부 요청을 Core의 Use Case로 전달합니다.

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

> 외부 요청은 Inbound Port를 통해 Core로 진입하고,
> 데이터 접근은 Outbound Port를 통해 Persistence Adapter로 위임됩니다.

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

## 📌 Core Domain Policy

### Transaction 시간 정책

Transaction은 거래 발생 시각과 시스템 데이터 생성 시각을 구분한다.

* `occurredAt`: 실제 거래가 발생한 시간
* `createdAt`: 시스템에서 데이터가 생성된 시간

`occurredAt`은 Core Domain의 비즈니스 데이터이며,
`createdAt`을 포함한 Audit 정보는 Persistence 계층에서 관리한다.

---

## 🚀 Quick Start

### 1. Clone

```bash
git clone https://github.com/jaehun-sys/money-log.git
cd money-log
````

### 2. Test

```bash
./gradlew test
```

### 3. Run API

```bash
./gradlew :api:bootRun
```

API 서버 실행 후:

```text
GET /api/v1/transactions
POST /api/v1/transactions
```

를 통해 현재 구현된 거래 API를 확인할 수 있습니다.

---

## 🗺️ Roadmap

- [x] Core Domain / Money / Transaction
- [x] Hexagonal Architecture 기반 Application Layer
- [x] JPA Persistence Adapter
- [x] 거래 등록 API
- [x] 기간별 거래 조회 API
- [ ] 거래 수정 / 삭제
- [ ] 인증 및 사용자 컨텍스트 연계
- [ ] 금융 데이터 정규화
- [ ] 자산 및 금융 분석 기능

상세한 개발 방향과 설계 원칙은 [`docs/PRODUCT.md`](docs/PRODUCT.md)를 참고하세요.

---

## 📚 Documentation

money-log의 상세한 개발 철학과 설계 원칙은 별도 문서로 관리합니다.

- **[PRODUCT.md](docs/PRODUCT.md)**  
  프로젝트의 목적, 도메인 모델링 원칙, 아키텍처 방향, 개발 방법론 및 로드맵

- **[Architecture Decision Records](docs/adr/)**  
  주요 아키텍처 및 도메인 설계 결정과 그 배경