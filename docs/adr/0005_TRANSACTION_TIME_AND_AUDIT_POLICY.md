# 0005. Transaction 시간 및 Audit 정보 분리 정책

## Status

Accepted

## Context

Transaction 도메인에 거래가 발생한 시간과 시스템에서 데이터가 생성된 시간을 구분할 필요가 있다.

가계부에서 사용자가 2026년 9월 20일에 결제한 거래를 9월 21일에 시스템에 등록할 수 있다.

이 경우 다음 두 시간은 서로 다른 의미를 가진다.

- 거래가 실제로 발생한 시간
- 시스템에 거래 데이터가 생성된 시간

기존 `timestamp`라는 이름만으로는 해당 시간이 비즈니스 이벤트의 발생 시각인지 시스템 데이터 생성 시각인지 의미가 명확하지 않았다.

또한 Transaction은 비즈니스 데이터를 표현하는 Core Domain 객체인데, 데이터 생성자나 생성 프로그램과 같은 시스템 Audit 정보까지 직접 책임지는 구조는 Domain과 Persistence의 책임을 혼합할 수 있다.

따라서 거래의 비즈니스 시간과 Persistence Audit 정보를 명확하게 분리한다.

## Decision

### 1. 거래 발생 시각은 `occurredAt`으로 표현한다.

`occurredAt`은 실제 거래가 발생한 비즈니스 시간을 의미한다.

```text
occurredAt
= 사용자가 기록하는 거래의 실제 발생 시각
````

예:

```text
거래 발생: 2026-09-20 12:30
시스템 등록: 2026-09-21 09:00

occurredAt = 2026-09-20 12:30
createdAt  = 2026-09-21 09:00
```

`occurredAt`은 Transaction의 핵심 비즈니스 데이터이므로 Core Domain에 포함한다.

---

### 2. 시스템 생성 시각은 `createdAt`으로 표현한다.

`createdAt`은 시스템에서 해당 데이터를 생성한 시각을 의미한다.

이는 거래가 실제로 발생한 시각과 동일하다고 가정하지 않는다.

따라서 다음과 같이 의미를 구분한다.

| 필드           | 의미            | 책임                  |
| ------------ | ------------- | ------------------- |
| `occurredAt` | 실제 거래 발생 시각   | Core Domain         |
| `createdAt`  | 시스템 데이터 생성 시각 | Persistence / Audit |

---

### 3. Transaction은 `Auditable`을 구현하지 않는다.

`createdAt`, `createdBy`, `createdPgmId`와 같은 Audit 정보는 Transaction의 핵심 비즈니스 의미가 아니라 시스템 데이터 관리에 필요한 Persistence metadata로 취급한다.

따라서 Core의 `Transaction`은 `Auditable` 인터페이스에 의존하지 않는다.

```text
Core

Transaction
├── id
├── userId
├── type
├── amount
├── category
├── memo
└── occurredAt
```

Persistence 계층에서는 필요한 Audit 정보를 Entity에 관리한다.

```text
Persistence

TransactionJpaEntity
├── business data
├── occurredAt
├── createdAt
├── createdBy
└── createdPgmId
```

이를 통해 Core Domain이 Persistence의 Audit 정책에 직접 의존하지 않도록 한다.

---

### 4. `userId`와 `createdBy`는 서로 다른 의미를 가진다.

`userId`는 해당 거래의 비즈니스 소유자를 의미한다.

`createdBy`는 시스템에서 해당 데이터를 생성한 행위자를 의미한다.

따라서 두 값을 동일한 의미로 취급하지 않는다.

현재 인증 기능이 구현되지 않았기 때문에 Persistence 계층에서는 임시로 다음 값을 사용한다.

```text
createdBy = SYSTEM_UNAUTH
```

이는 인증되지 않은 실제 사용자를 의미하는 최종 정책이 아니라, 인증 기능 도입 전까지의 임시 구현이다.

---

### 5. 프로그램 식별자는 Persistence 경계에서 관리한다.

`createdPgmId`는 거래 자체의 비즈니스 데이터가 아니라 데이터를 생성한 시스템/프로그램을 식별하기 위한 Audit metadata이다.

따라서 Core Application Service에서 이를 직접 생성하지 않고 Persistence 경계에서 주입한다.

현재 API 서버의 프로그램 식별자는 다음 값을 사용한다.

```text
createdPgmId = MONEY_LOG_API
```

## Consequences

### Positive

* 거래 발생 시간과 시스템 생성 시간을 명확하게 구분할 수 있다.
* 과거 거래를 사후 입력하는 경우에도 실제 거래 발생 시각을 보존할 수 있다.
* Core Domain이 Persistence Audit 정책에 직접 의존하지 않는다.
* 향후 인증 기능이 추가되어도 `createdBy` 정책을 Persistence 경계에서 확장할 수 있다.
* 거래 조회에서 `occurredAt`을 기준으로 기간 조회 정책을 명확하게 정의할 수 있다.

### Negative

* 비즈니스 시간과 시스템 시간이 서로 다른 필드로 관리되므로 데이터 모델이 단순하지 않다.
* Audit 정보가 Core Domain에 존재하지 않기 때문에 Core만으로 생성자 정보를 알 수 없다.
* 인증 기능이 구현되기 전까지 `createdBy`는 임시 값으로 관리된다.
* 기간 조회 API의 입력 조건만 Core에 정의되어 있고, 실제 Persistence 조회 조건은 아직 구현되지 않았다.

## Related Components

* `core/domain/Transaction.kt`
* `core/application/port/inbound/RecordTransactionCommand.kt`
* `storage/persistence/entity/TransactionJpaEntity.kt`
* `storage/persistence/adapter/TransactionPersistenceAdapter.kt`

## Future Considerations

인증/인가 기능이 추가되면 현재 `SYSTEM_UNAUTH`로 처리하는 `createdBy`를 인증된 사용자 또는 시스템 행위자 정보로 대체한다.

또한 거래 수정 기능이 구현될 경우 `updatedAt`, `updatedBy`, `updatedPgmId`의 생성 및 관리 책임 역시 동일한 Persistence/Audit 경계에서 정의한다.