# Persistence Package Specification

## 1. 패키지 격리의 근거
`:storage` 모듈 하위에 `persistence` 패키지를 명시적으로 분리한 이유는 **'저장 매체의 성격에 따른 책임 분리'**를 위해서다. 향후 본 모듈에는 다음과 같은 패키지들이 병렬로 추가될 수 있다.
* `com.moneylog.storage.cache` (Redis 기반 조회 성능 최적화)
* `com.moneylog.storage.file` (영수증 OCR 이미지 S3 저장)

따라서 RDB 기반의 영속성(Persistence) 제어 로직을 본 패키지 하위로 가두어 다른 인프라 기술과의 관심사 섞임을 원천 차단한다.

---

## 2. 하위 패키지 및 핵심 부품의 역할

### ① `entity/` (RDB 패러다임 브릿지)
* **주요 클래스:** `TransactionJpaEntity`
* **역할:** 관계형 데이터베이스의 테이블 스키마와 1:1로 매핑되는 데이터 트럭.
* **구현 논리:** 코어 도메인의 객체 그래프를 테이블의 평면적 구조(Columns)로 펼쳐서 담는다. 금액 파손을 막기 위해 DB 표준 데이터 규격인 `numeric(19,4)`와 문자열 통화 코드(`varchar(3)`)로 분리 매핑했다.

### ② `repository/` (SQL 쿼리 생성 엔진)
* **주요 인터페이스:** `TransactionJpaRepository`
* **역할:** Spring Data JPA 프레임워크의 힘을 빌려 상투적인 CRUD SQL 작성을 자동화하는 일꾼.

### ③ `adapter/` (도메인-인프라 통역사)
* **주요 클래스:** `TransactionPersistenceAdapter`
* **역할:** `:core`의 `SaveTransactionPort`를 구현하는 실제 문지기.
* **구현 논리:** 1. 무균실의 `Transaction` 도메인 객체를 파라미터로 받는다.
    2. 이를 DB 트럭인 `TransactionJpaEntity`로 완벽하게 **번역(Mapping)**한다.
    3. 일꾼(`Repository`)에게 트럭을 넘겨 DB에 `INSERT` 시킨다.

---

## 3. 확장성을 고려한 설계 흔적
* **통화(Currency)의 안전한 영속화:** 화폐 도메인(`Money`)을 DB에 넣을 때 단순 소수점 숫자 하나로 퉁치지 않고 `amount`와 `currency_code` 두 개의 컬럼으로 완전 분리했다. 이로 인해 향후 미국 달러(USD), 일본 엔화(JPY) 다중 통화 가계부로 확장될 때 DB 스키마 변경이 전혀 필요 없도록 설계되었다.