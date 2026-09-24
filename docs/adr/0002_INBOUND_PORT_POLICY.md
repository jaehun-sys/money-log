# 🚪 Money-Log 인바운드 포트(Use Case) 설계 정책

이 문서는 Money-Log 프로젝트의 애플리케이션 계층, 그중에서도 외부 요청이 코어 도메인으로 들어오는 정문(Inbound Port)을 설계할 때의 합의된 원칙을 명시합니다.

## 1. Use Case 인터페이스 분리 (Inbound Port)
- **행위의 추상화:** 외부(Web, App 등)에는 구체적인 서비스 구현체를 노출하지 않고, 시스템이 제공하는 '비즈니스 행위(What)'만을 정의한 인터페이스(Use Case)를 노출합니다.
- **도메인 네이밍 (Ubiquitous Language):** `create`, `insert` 등 데이터베이스 중심의 CRUD 용어를 배제하고, `record`(기록하다)와 같은 현업의 비즈니스 언어를 메서드명으로 사용합니다.

## 2. 전용 Command 객체 활용 (계층 격리)
- **프레임워크 오염 방지:** Spring Web의 `@RequestBody`용 DTO를 애플리케이션 계층으로 절대 침투시키지 않습니다. 컨트롤러는 반드시 웹의 언어를 코어의 언어인 `Command` 객체로 변환해서 유즈케이스로 전달해야 합니다.
- **CQRS 명명 규칙:** 데이터의 상태를 변경하는(Write) 요청은 `Command`로, 상태를 읽기만 하는(Read) 요청은 `Query`로 명명하여 객체의 목적을 명확히 분리합니다.

## 3. 자가 검증 커맨드 (Self-Validating Command)
- **Fail-Fast (빠른 실패) 원칙:** `Command` 객체는 생성되는 즉시 코틀린의 `init` 블록과 `require`를 통해 데이터의 형식적 유효성(Syntax Validation)을 스스로 검증합니다.
- **책임의 완벽한 분리:** 빈 값, 음수, 길이 초과 등의 단순 쓰레기 데이터는 입구(`Command`)에서 차단합니다. 이를 통해 코어 도메인(`Transaction`, `Money`)은 지저분한 방어 코드 없이 순수 비즈니스 규칙(Semantic Validation) 검증에만 집중할 수 있습니다.

## 4. Command와 Query의 구분

Inbound Port는 외부에서 수행하려는 하나의 Use Case를 표현한다.

요청의 목적에 따라 입력 모델을 Command와 Query로 구분한다.

### Command

Command는 시스템의 상태를 변경하는 요청을 표현한다.

현재 거래 등록은 다음 구조를 사용한다.

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
````

`RecordTransactionCommand`는 거래 등록에 필요한 비즈니스 입력을 표현하며, Core Application 계층에서 필요한 불변 조건을 검증한다.

### Query

Query는 시스템의 상태를 변경하지 않고 데이터를 조회하기 위한 요청을 표현한다.

현재 거래 기간 조회는 다음 구조를 사용한다.

```text
HTTP Request
    ↓
LoadTransactionsQuery
    ↓
LoadTransactionsUseCase
    ↓
LoadTransactionsService
```

`LoadTransactionsQuery`는 다음 조회 조건을 표현한다.

* `userId`
* `startDate`
* `endDate`

조회 기간은 `startDate <= endDate` 조건을 만족해야 한다.

### Policy

새로운 Inbound Use Case를 추가할 때 Command와 Query의 목적을 명확하게 구분한다.

* 상태 변경 → `Command`
* 상태 조회 → `Query`

Command와 Query는 각각 독립적인 UseCase 계약을 통해 Application Service로 전달한다.

HTTP Request DTO를 Core의 Command 또는 Query로 직접 사용하지 않는다.

Web Adapter의 입력 모델과 Core Application의 입력 모델을 분리하여 외부 프로토콜과 Core의 결합을 방지한다.