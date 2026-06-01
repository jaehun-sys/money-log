# 💰 money-log (금융 비즈니스 코어 엔진)

> **"외부 프레임워크와 데이터베이스 기술에 종속되지 않는 가장 순수한 금융 도메인의 심장부"**

본 프로젝트는 비즈니스 규칙이 스스로를 보호하는 **헥사고날 아키텍처(Hexagonal Architecture)** 및 **DDD(도메인 주도 설계)** 기반의 고신뢰성 금융 연산 엔진입니다.

---

## 🏗️ 아키텍처 아키타입 (Multi-Module System)

본 시스템은 Gradle 멀티 모듈을 통해 기술과 비즈니스의 경계를 물리적으로 완벽히 격리합니다.

* `:core` - **비즈니스 심장부 (Pure Kotlin, Java 21)**. Spring Boot, JPA 등 일체의 외부 프레임워크 의존성을 배제하고 오직 순수 도메인 규칙과 값 객체(VO)만 존재합니다.
* `:storage` - 데이터 영속성 어댑터 레이어 (Spring Data JPA, PostgreSQL).
* `:api` - 웹 인바운드 어댑터 레이어 (Spring Boot Web, REST API).

---

## 🛠️ 기술 스택 및 환경 (Tech Stacks)
* **Language:** Kotlin 1.9.22 (Strict Null-Safety)
* **JDK:** Eclipse Temurin Java 21 (LTS)
* **Build Tool:** Gradle Wrapper (Kotlin DSL)
* **Architecture:** Hexagonal (Ports and Adapters) / Domain-Driven Design