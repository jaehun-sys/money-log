# 🏛️ Money-Log API 모듈 설계 및 아키텍처 정책

본 문서는 `money-log` 프로젝트의 `:api` 모듈(Web Adapter)을 구축하며 정의한 핵심 설계 정책과 헥사고날 아키텍처 경계 규칙을 명문화한 것이다. 본 정책은 코드(Implementation), 테스트(Test), 문서(README) 간의 완벽한 동기화(SSOT)를 지향한다.

```text
[ 🛡️ Web Adapter 3단계 방어선 및 데이터 흐름 ]

(Client) HTTP JSON
   │
   ├─ 1. Web Validation ── @Valid (HTTP 구문 및 API 입력 제약 검증)
   ▼                        
RecordTransactionRequest (DTO)
   │
   ├─ 2. Translation ───── Controller가 기본값 확정 및 정규화 적용
   ▼
RecordTransactionCommand (Command)
   │
   ├─ 3. Input Invariant ─ init { require(...) } (UseCase 실행 불변조건 강제)
   ▼                        
[ UseCase Port ]
   │
[ Application Service ] ── ❌ Domain Rule 위반 (IllegalArgumentException)
                               └─> GlobalExceptionHandler가 HTTP 400으로 변환

```

## 1. 계층별 검증 책임의 명확한 분리 (Validation Boundaries)

유효성 검증은 단일 계층에서 몰아서 하지 않으며, 각 객체의 책임에 맞게 방어선을 구축한다.

* **Web Validation (`RecordTransactionRequest` DTO):**
* **책임:** HTTP 요청의 형식과 API 입력 제약이 유효한가?
* **구현:** Kotlin의 Nullable(`?`) 타입과 Spring의 `@Valid`(`@NotNull`, `@Positive`, `@NotBlank`) 어노테이션을 조합하여 Jackson 파싱 에러를 방지하고 에러를 수집한다.


* **Command Invariant (`RecordTransactionCommand`):**
* **책임:** UseCase가 실행되기 전에 애플리케이션 입력 불변조건을 보장하는가?
* **구현:** `init { require(...) }` 블록을 통해, 생성되는 즉시 스스로 데이터를 검증한다.


* **Domain Validation (`Transaction`, `Money`):**
* **책임:** 실제 도메인 비즈니스 규칙을 만족하는가? (코어 모델 내부에 캡슐화)



## 2. API 숫자 타입 정책: `BigDecimal` 강제

금융 도메인의 정밀도를 보장하기 위해, API 경계선에서 부동소수점(`Double`)의 사용을 엄격히 금지한다.

* JSON의 숫자 값은 Jackson을 통해 `BigDecimal` 프로퍼티로 역직렬화된다.
* 불필요한 `Double` 변환 과정을 없애고, `Request(BigDecimal)` -> `Command(BigDecimal)` -> `Money(BigDecimal)`의 흐름으로 일관성을 유지한다.

## 3. Web Adapter의 책임 제한 (도메인 조립 금지)

* **원칙:** Controller는 도메인 객체(`Money`, `Transaction`)의 존재를 철저히 몰라야 한다.
* **구현:** Controller는 HTTP JSON을 DTO로 받은 뒤, 이를 순수한 기본 타입으로 구성된 Command로 '번역'하여 UseCase에 전달할 뿐이다.
* **이점:** 향후 CLI, Kafka Consumer 등 새로운 인바운드 어댑터가 추가되어도, 도메인 조립(`Money.of()`, `Transaction()`) 로직이 각 어댑터에 중복되지 않고 Application Service에 단일화된다.

## 4. 시스템 감사(Audit) 메타데이터 및 식별자 처리 정책

* **현재 정책 (Phase 4):** 클라이언트가 전달한 `userId`를 Request와 Command를 통해 Application Service까지 전달하고, Service 계층에서 `createdBy`와 `createdPgmId("MONEY_LOG_API")`를 주입하여 영속화한다.
* **미래 방향성:** 향후 인증 시스템이 도입되면, 클라이언트의 `userId` 명시적 전달을 폐지하고 Web Adapter가 Security Context에서 인증된 사용자 식별자를 추출하여 Command에 담도록 구조를 진화시킨다.

## 5. Timestamp 기본값 처리

* **정책:** 클라이언트가 `timestamp`를 생략한 경우, Controller가 `LocalDateTime.now()`를 호출하여 서버 처리 시점의 거래 시간을 확정한 뒤 Command에 주입한다.
* **의도:** Command의 `timestamp` 필드를 Non-null(`val`)로 유지하여, '모든 값이 확정된 완벽한 명령서'라는 Command의 불변성을 보장하기 위함이다.

## 6. 통화(Currency) 정규화 및 예외 번역

* **정규화:** 클라이언트가 보낸 통화 코드를 대문자로 변환할 때, `uppercase(Locale.ROOT)`를 강제하여 OS/지역 설정에 따른 글로벌 서비스 버그를 원천 차단한다.
* **예외 번역:** Application Service에서 통화 코드 검증(`Currency.getInstance`) 중 발생한 `IllegalArgumentException`은 Controller가 직접 처리하지 않는다. `@RestControllerAdvice`(`GlobalExceptionHandler`)가 이를 낚아채어 HTTP 400 (Bad Request)으로 변환한다.

## 7. 테스트 환경 정책 (@WebMvcTest)

* **정책 배경:** `@WebMvcTest` 환경에서 `springmockk`의 `@MockkBean`을 사용할 때, JUnit 5의 생성자 파라미터 주입 과정에서 `ParameterResolutionException`이 발생하는 기술적 한계가 존재한다.
* **프로젝트 표준:** 따라서 본 프로젝트의 WebMvcTest 클래스에서는 `MockMvc`, `ObjectMapper`, `@MockkBean` 의존성에 대해 생성자 주입을 피하고, `lateinit var`를 이용한 필드 주입 방식을 표준으로 채택한다.

