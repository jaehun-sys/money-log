# 🚪 Money-Log 아웃바운드 포트(Repository Interface) 설계 정책

이 문서는 Money-Log 프로젝트의 코어 도메인이 외부 인프라(DB, 외부 API 등)로 데이터를 내보내기 위해 통과해야 하는 뒷문(Outbound Port)을 설계할 때의 합의된 원칙을 명시합니다.

## 1. DIP (의존성 역전 원칙) 적용
- **코어가 인프라를 지배한다:** 아웃바운드 포트는 철저히 코어 모듈(`core/`) 내부에 위치합니다. 인터페이스 설계 시 JPA, MySQL, Mongo 등 특정 데이터베이스 기술을 암시하는 용어나 어노테이션을 일절 배제합니다.
- **인프라는 그저 부품일 뿐이다:** 인프라 계층(`storage/`)은 코어 계층이 정의한 이 인터페이스(Port) 규격에 맞춰 어댑터(Adapter)를 구현함으로써, 언제든 다른 기술로 갈아 끼울 수 있는 유연성을 확보합니다.

## 2. 네이밍 규칙: Repository 대신 Port
- **레거시 오염 차단:** `Repository`라는 단어가 주는 강력한 데이터베이스(DB) 중심적 사고를 끊어내기 위해 `Port`라는 아키텍처 용어를 사용합니다.
- **플러그 앤 플레이:** 이 인터페이스는 데이터베이스뿐만 아니라 메시지 큐(Kafka), 외부 API, 파일 시스템 등 어떤 외부 기술과도 연결될 수 있는 '추상적인 규격(콘센트)'임을 명확히 합니다.

## 3. ISP (인터페이스 분리 원칙) 준수
- **목적에 맞는 최소한의 문:** 하나의 거대한 인터페이스에 CRUD(생성, 조회, 수정, 삭제)를 모두 때려 넣지 않습니다.
- 현재 유즈케이스가 요구하는 행위(예: `Save`)만을 인터페이스로 분리하여, 개발자가 의도치 않게 다른 데이터 조작(예: `findById`)을 하는 실수를 아키텍처 레벨에서 차단합니다.

## 4. 영속화 상태 보장 (Return Type)
- **void/Unit 사용 지양:** 데이터를 저장하는 행위(`save`)는 반환 타입을 생략하지 않고, 반드시 영속화가 완료된 도메인 객체(`Transaction`)를 다시 반환하도록 계약(Contract)을 맺습니다.
- 이를 통해 유즈케이스 호출자에게 저장 성공 여부와 최종 상태를 명확히 보장하며, 불변성을 해치지 않는 체이닝 구현을 가능하게 합니다.

## 5. 조회용 Outbound Port

Outbound Port는 저장뿐만 아니라 Core가 외부 데이터 저장소에서 필요한 데이터를 조회하기 위한 계약도 정의한다.

거래 조회 기능에서는 다음 Port를 사용한다.

```kotlin
interface LoadTransactionsPort {
    fun load(query: LoadTransactionsQuery): List<Transaction>
}
````

Core는 JPA Repository나 SQL Query를 직접 참조하지 않는다.

구조는 다음과 같다.

```text
LoadTransactionsService
        ↓
LoadTransactionsPort
        ↓
TransactionPersistenceAdapter
        ↓
TransactionJpaRepository
        ↓
Database
```

### Query 객체의 소유권

조회 조건을 표현하는 `LoadTransactionsQuery`는 Core Application 계층에서 정의한다.

따라서 Persistence Adapter가 사용하는 JPA Specification, QueryDSL, JPQL 등의 기술적인 조회 방식은 Core에 노출하지 않는다.

Core가 요구하는 것은 다음과 같은 비즈니스 조회 조건이다.

```text
userId
startDate
endDate
```

실제 DB 조회 조건으로 변환하는 책임은 Persistence Adapter가 가진다.

### Policy

새로운 외부 데이터 조회가 필요한 경우:

1. Core Application에서 UseCase를 정의한다.
2. 필요한 조회 조건을 Query 모델로 정의한다.
3. Core에 Outbound Port를 정의한다.
4. Persistence Adapter에서 Port를 구현한다.
5. 실제 DB 조회 기술은 Adapter 내부에서 결정한다.

이를 통해 Core가 특정 Persistence 기술에 종속되지 않도록 한다.