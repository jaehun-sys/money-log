# 🏴‍☠️ :storage Module (영속성 계층 인프라 어댑터)

## 📌 개요
본 모듈은 헥사고날 아키텍처(Hexagonal Architecture)의 **외부 영속성 어댑터(Persistence Adapter)** 영역입니다.
애플리케이션의 핵심 비즈니스 로직이 담긴 무균실(`:core` 모듈)을 외부 기술(JPA, 특정 RDB 등)로부터 철저하게 격리하고 보호하기 위해 독립된 서브 모듈로 분리되었습니다.

---

## 🧭 아키텍처 원칙 (Dependency Inversion)
- **의존성 방향:** `:storage` ──> `:core` (역방향 의존 금지)
- `:core` 모듈은 특정 데이터베이스 기술을 알지 못하며, 오직 인바운드/아웃바운드 포트(인터페이스)만 정의합니다.
- `:storage` 모듈은 `:core`가 정의한 `Outbound Port`를 기술적으로 **구현(Implements)**하여 영속성을 부여하는 진흙탕(인프라) 역할을 수행합니다.
- 이를 통해 향후 핵심 로직의 변경 없이 RDB(PostgreSQL)에서 NoSQL 또는 다른 저장소 기술로의 유연한 교체가 가능하도록 설계되었습니다.

---

## 🛠️ 빌드 환경 및 기술 스택
- **Framework:** Spring Boot Starter Data JPA (3.2.4)
- **Language Plugin:** Kotlin plugin.spring / plugin.jpa (1.9.22)
- **Local Database:** H2 Database (Runtime) -> *추후 PostgreSQL 전환 예정*

---

## 🚨 오픈소스 기여자 및 온보딩 개발자를 위한 트러블슈팅 가이드

### 1. `io.spring.dependency-management` 플러그인 버전 호환성 (NoSuchMethodError)
- **현상:** Gradle 9.x 대 이상 환경에서 빌드 시 `java.lang.NoSuchMethodError: ... LenientConfiguration.getArtifacts` 에러와 함께 빌드가 실패하는 현상.
- **원인:** 구형 의존성 관리 플러그인(`1.1.4` 이하)이 최신 Gradle API와 호환되지 않아 발생합니다.
- **해결:** 본 프로젝트의 `:storage` 모듈은 이를 방지하기 위해 Gradle 최신 사양과 호환되는 `1.1.5` 버전을 명시하여 해결했습니다. 빌드 툴 체인 변경 시 유의하시기 바랍니다.

### 2. Multi-Module 환경에서의 실행 파일(BootJar) 제어
- 본 모듈은 단독으로 실행되는 애플리케이션이 아닌 독립된 '라이브러리/부품' 모듈입니다.
- 실행 가능한 JAR 생성을 방지하고 컴파일 에러를 막기 위해 아래 설정을 강제하고 있습니다.
  ```kotlin
  tasks.bootJar { enabled = false }
  tasks.jar { enabled = true }