# 🧪 Storage 모듈 테스트 전략 (Persistence Slice Test)

## 📌 1. 테스트 아키텍처 개요
본 모듈의 테스트는 시스템 전체를 무겁게 띄우는 `@SpringBootTest`를 철저히 배제하고, 오직 JPA와 영속성 계층의 부품들만 메모리에 띄우는 **영속성 슬라이스 테스트(Persistence Slice Test, `@DataJpaTest`)** 전략을 채택합니다.
이를 통해 수 초(Seconds) 내에 초고속으로 인프라 계층의 무결성을 검증합니다.

---

## 🎯 2. 핵심 검증 논리 (What to Test)

테스트의 주 목적은 비즈니스 로직(Core)의 검증이 아닙니다. **"무균실(도메인)의 데이터가 진흙탕(DB)의 규격으로 오차 없이 번역되어 들어가는가?"**를 증명하는 데 집중합니다.

1. **객체 번역 무결성 (통역사 검증):** - `TransactionPersistenceAdapter`가 순수 도메인 객체(`Transaction`)를 `TransactionJpaEntity`로, 또는 그 역방향으로 손실 없이 완벽하게 매핑하는지 검증합니다.
2. **DDL 및 스키마 매핑 (진흙탕 규격 검증):**
    - `@Column`의 제약조건(`nullable`, `length`, `precision`)이 실제 DB 스키마(DDL)에 정확하게 적용되어 테이블이 생성되는지 확인합니다.
3. **Repository 쿼리 실행:**
    - Spring Data JPA가 생성한 쿼리가 문법적 오류 없이 정상적으로 `INSERT/SELECT` 되는지 확인합니다.

---

## 🚧 3. 테스트의 가정과 경계 (Assumptions & Boundaries)

- **비즈니스 로직 배제:** `:core`의 비즈니스 룰은 이미 완벽하다고 가정합니다. 테스트를 위한 `Transaction` 도메인 객체는 단순히 인프라 계층을 통과하기 위한 더미(Dummy) 데이터로 취급됩니다.
- **인메모리 DB 활용:** 빠른 피드백 루프를 위해 로컬 테스트는 `H2` 인메모리 데이터베이스를 사용합니다. (특정 RDB에 종속적인 네이티브 쿼리는 배제하며, 추후 PostgreSQL 고유 기능 필요 시 `Testcontainers` 기반으로 전환을 고려합니다.)

---

## 🛠️ 4. 특수 인프라 설정 (Troubleshooting Guide)

본 프로젝트는 멀티 모듈 기반이므로, `@DataJpaTest` 구동 시 스프링이 길을 잃지 않도록 아래와 같은 특수 장치들이 셋업되어 있습니다. 기여자들은 테스트 환경 구성 시 이를 반드시 인지해야 합니다.

1. **가짜 심장 (`StorageTestApplication.kt`):**
    - `:storage` 모듈은 단독 실행 애플리케이션이 아니므로, 테스트 컨텍스트를 띄우기 위한 빈(Empty) `@SpringBootApplication` 클래스를 테스트 패키지 최상단에 배치했습니다.
2. **경로 강제 명시 (`@EntityScan`, `@EnableJpaRepositories`):**
    - 테스트 클래스 상단에 엔티티와 레포지토리의 패키지 경로를 강제로 꽂아넣어, 빈(Bean) 스캔 누락으로 인한 `UnsatisfiedDependencyException`을 방지합니다.
3. **Gradle 9.x 호환성 (`junit-platform-launcher`):**
    - 최신 Gradle 환경에서 JUnit 5 기반 스프링 부트 테스트가 뻗는 현상을 막기 위해, `build.gradle.kts`에 런타임 런처 의존성을 명시적으로 포함하고 있습니다.