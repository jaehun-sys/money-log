# money-log Phase 5.1: 월별 지출 분석

## Financial Analytics Specification

**작성일:** 2026-09-29
**상태:** Confirmed Specification / Technical Decisions Pending
**대상 Phase:** Phase 5 — Financial Analytics
**문서 경로:** `docs/specs/PHASE5_ANALYTICS.md`

## 목차

- [money-log Phase 5.1: 월별 지출 분석](#money-log-phase-51-월별-지출-분석)
- [1. 문서의 목적](#1-문서의-목적)
- [2. 결정 상태의 정의](#2-결정-상태의-정의)
- [3. Decision Summary](#3-decision-summary)
- [4. Phase 5.1 분석 목표](#4-phase-51-분석-목표)
- [5. Scope Boundary](#5-scope-boundary)
- [6. 현재 모델의 의도적인 한계](#6-현재-모델의-의도적인-한계)
- [7. 금융 도메인 규칙](#7-금융-도메인-규칙)
- [8. 시간 정책](#8-시간-정책)
- [9. 다중 통화 정책](#9-다중-통화-정책)
- [10. 카테고리 분석](#10-카테고리-분석)
- [11. Ratio 정책](#11-ratio-정책)
- [12. '미분류' 데이터 정책](#12-미분류-데이터-정책)
- [13. 결과 정렬](#13-결과-정렬)
- [14. Empty Result](#14-empty-result)
- [15. 분석 결과 모델](#15-분석-결과-모델)
- [16. Architecture Responsibility](#16-architecture-responsibility)
- [17. Core 인메모리 연산](#17-core-인메모리-연산)
- [18. 분석 데이터 공급 Port](#18-분석-데이터-공급-port)
- [19. Port 설계 원칙](#19-port-설계-원칙)
- [20. Application Service 책임](#20-application-service-책임)
- [21. Core 내부 구조](#21-core-내부-구조)
- [22. 구현 시 예상 흐름](#22-구현-시-예상-흐름)
- [23. Persistence 구현](#23-persistence-구현)
- [24. API 설계](#24-api-설계)
- [25. 테스트 전략](#25-테스트-전략)
- [26. 구현 과업](#26-구현-과업)
- [27. 구현하지 말아야 할 것](#27-구현하지-말아야-할-것)
- [28. 향후 확장 후보](#28-향후-확장-후보)
- [29. Definition of Done](#29-definition-of-done)
- [30. 문서 관리 원칙](#30-문서-관리-원칙)
- [31. 최종 설계 원칙](#31-최종-설계-원칙)

# 1. 문서의 목적

Phase 5는 `Transaction` 데이터를 기반으로 사용자의 금융 상태를 분석하고 의미 있는 정보를 제공하는 첫 번째 단계다.

금융 분석은 단순한 `SUM`, `GROUP BY` 또는 화면 조회 기능으로 취급하기 어렵다.

같은 거래 데이터라도 다음과 같은 정책에 따라 분석 결과가 달라진다.

* 어떤 거래를 지출로 간주하는가?
* 거래 발생일과 시스템 등록일 중 어떤 시간을 기준으로 하는가?
* 서로 다른 통화를 어떻게 취급하는가?
* 카테고리별 비중을 어떻게 계산하고 반올림하는가?
* 내부 계좌 이체를 지출에 포함할 것인가?
* 환불이나 취소를 지출에서 어떻게 차감하는가?
* 이러한 계산을 DB에서 수행할 것인가, 애플리케이션에서 수행할 것인가?

이러한 정책을 구현 단계에서 임의로 결정하면 **코드가 곧 금융 도메인 정책의 문서가 되어 버리고**, 이후 요구사항 변경 시 정책의 근거를 추적하기 어려워진다.

따라서 이 문서는 Phase 5.1 구현에 앞서 다음을 명확히 정의한다.

1. 이번 단계에서 어떤 분석을 제공하는가
2. 이번 단계에서 어떤 문제를 의도적으로 해결하지 않는가
3. 분석 결과의 금융적 의미는 무엇인가
4. 금융 계산의 책임은 어느 계층에 있는가
5. 현재 `Transaction` 모델의 어떤 한계를 허용하는가
6. 아직 결정하지 않은 기술적 선택은 무엇인가
7. 향후 어떤 문제를 별도의 도메인 과제로 다룰 것인가

이 문서는 향후 팀원이 합류하거나 코드를 다시 열었을 때,

> **"왜 이렇게 구현했는가?"**

라는 질문에 답하기 위한 Phase 5.1의 기준 문서다.

---

# 2. 결정 상태의 정의

이 문서에서는 모든 내용을 동일한 수준의 결정으로 취급하지 않는다.

## 2.1. Confirmed — 확정

Phase 5.1의 금융적 의미와 Scope를 정의하는 **확정된 요구사항 및 설계 원칙**이다.

구현 과정에서 임의로 변경하지 않는다.

변경이 필요하다면 이 문서의 결정 목록을 수정하고, 필요한 경우 ADR에 변경 이유를 기록한다.

---

## 2.2. Pending — 미확정

Phase 5.1 구현 과정에서 구체적인 기술 설계를 결정해야 하는 항목이다.

현재 문서는 해당 항목의 **방향과 제약사항만 정의하며, 구체적인 구현 방법은 고정하지 않는다.**

따라서 Pending 항목을 구현자가 임의로 확정된 정책으로 간주해서는 안 된다.

---

## 2.3. Deferred — 이번 단계에서 연기

현재 요구사항에서 의도적으로 제외한 문제다.

단순히 아직 구현하지 못한 것이 아니라, **Phase 5.1의 Scope 밖에 두기로 결정한 문제**다.

향후 별도의 요구사항 및 설계 검토를 통해 다룬다.

---

# 3. Decision Summary

이 장은 Phase 5.1의 **결정 사항에 대한 단일 기준(SSOT)** 이다.

본문의 각 섹션은 아래 결정을 구체적으로 설명하며, 동일한 결정을 별도의 표에서 반복하지 않는다.

## 3.1. Confirmed

| 영역             | 확정 내용                                    |
| -------------- | ---------------------------------------- |
| 분석 대상          | `WITHDRAWAL`                             |
| 지출 의미          | Gross Expense                            |
| 시간 기준          | `occurredAt`                             |
| 월간 집계          | `YearMonth`                              |
| 기간 경계          | `[start, end)`                           |
| 현재 Timezone 정책 | `Asia/Seoul`                             |
| 통화             | Currency별 독립 집계                          |
| 환율 변환          | 하지 않음                                    |
| 카테고리           | 카테고리별 금액 및 비중                            |
| Ratio 타입       | `BigDecimal`                             |
| Ratio Scale    | 2                                        |
| Rounding       | `HALF_EVEN`                              |
| Ratio 100% 보정  | 하지 않음                                    |
| 미분류            | Ingestion / Normalization에서 `"미분류"`로 정규화 |
| `category` 불변식 | `isNotBlank()` 유지                        |
| 내부 이체          | Phase 5.1에서 별도로 제외하지 않음                  |
| 금융 계산 책임       | Core                                     |
| 데이터 공급 책임      | Storage                                  |
| 현재 계산 방식       | Transaction을 조회한 뒤 Core에서 인메모리 계산        |
| 결과             | Immutable Read Model                     |
| 정렬             | amount DESC → category ASC               |
| 거래 없음          | 빈 결과                                     |
| 성능 최적화         | 실제 측정 후 재검토                              |

---

## 3.2. Pending

| 항목                  | 결정할 내용                                            |
| ------------------- | ------------------------------------------------- |
| 분석용 Port            | 기존 Port 재사용 또는 별도 Port 도입                         |
| 분석용 Port 계약         | 정확한 메서드 및 데이터 범위                                  |
| Core 계산 구조          | Domain Service / Calculator 등 구체적 구조              |
| Read Model 위치       | 정확한 모듈 및 패키지                                      |
| Ratio 내부 표현         | Percentage / Fraction                             |
| Timezone 구현         | `ZoneId`, 시스템 설정, 시간 추상화 등                        |
| Application Service | 구체적인 클래스 및 메서드 계약                                 |
| Persistence Query   | JPA Query Method / JPQL / QueryDSL / Native SQL 등 |
| API                 | Endpoint / Request / Response 계약                  |

---

## 3.3. Deferred

| 항목                        | 상태       |
| ------------------------- | -------- |
| Net Expense               | Deferred |
| Refund / Cancellation     | Deferred |
| Partial Refund            | Deferred |
| Dutch Pay / 비용 분담         | Deferred |
| Internal Transfer 식별 및 제외 | Deferred |
| User-specific Timezone    | Deferred |
| FX Conversion             | Deferred |
| Base Currency 통합 분석       | Deferred |
| 자동 카테고리 분류                | Deferred |
| 대규모 DB Aggregation 최적화    | Deferred |
| Pre-aggregation           | Deferred |
| 분석 결과 Cache               | Deferred |
| 대규모 성능 최적화                | Deferred |

---

# 4. Phase 5.1 분석 목표

Phase 5.1의 목표는 사용자의 특정 월 지출을 **통화별·카테고리별로 분석하여 읽기 가능한 리포트로 제공하는 것**이다.

현재 `Transaction` 모델에서 표현 가능한 `WITHDRAWAL` 거래를 **Gross Expense(총지출)**로 취급한다.

예:

| 날짜         | 유형         | 통화  | 카테고리 |        금액 |
| ---------- | ---------- | --- | ---- | --------: |
| 2026-09-03 | WITHDRAWAL | KRW | 식비   |    30,000 |
| 2026-09-05 | WITHDRAWAL | KRW | 교통   |    10,000 |
| 2026-09-10 | WITHDRAWAL | KRW | 식비   |    20,000 |
| 2026-09-12 | DEPOSIT    | KRW | 급여   | 3,000,000 |
| 2026-09-15 | WITHDRAWAL | USD | 쇼핑   |       100 |

결과:

```text
KRW
총지출: 60,000 KRW
- 식비: 50,000 KRW (83.33%)
- 교통: 10,000 KRW (16.67%)

USD
총지출: 100 USD
- 쇼핑: 100 USD (100.00%)
```

KRW와 USD는 환율을 이용하여 하나의 금액으로 합산하지 않는다.

---

# 5. Scope Boundary

## 5.1. In Scope

| 영역       | 구현 범위                              |
| -------- | ---------------------------------- |
| 지출 정의    | `WITHDRAWAL`을 Gross Expense로 집계    |
| 시간 기준    | `Transaction.occurredAt`           |
| 기간       | 특정 `YearMonth`                     |
| 기간 경계    | `[start, end)`                     |
| Timezone | `Asia/Seoul` 기준으로 해석               |
| 통화       | Currency별 독립 집계                    |
| 카테고리     | 카테고리별 지출액 및 비중                     |
| 비중       | 총지출 대비 카테고리 지출 비율                  |
| 계산 책임    | Core                               |
| 데이터 공급   | Storage                            |
| 결과       | 불변 Read Model                      |
| 결과 정렬    | 금액 DESC → 카테고리 ASC                 |
| 소수 처리    | `BigDecimal`, Scale 2, `HALF_EVEN` |

---

## 5.2. Out of Scope

다음은 Phase 5.1에서 구현하지 않는다.

| 항목                      | 제외 이유                               |
| ----------------------- | ----------------------------------- |
| Net Expense             | 환불·취소·부분 환급 등의 금융 의미 정의 필요          |
| 환불/취소 상쇄                | `DEPOSIT`과 `WITHDRAWAL` 간 경제적 관계 필요 |
| 더치페이 정산                 | 개인 간 비용 분담이라는 별도 도메인 필요             |
| 내부 이체 제외                | Transfer와 Expense를 구분할 금융 이벤트 모델 필요 |
| 사용자별 Timezone           | 거래 시각 의미 및 기간 정책 확장 필요              |
| 환율 변환                   | FX Rate 및 기준 통화 정책 필요               |
| 통합 자산 분석                | 통화별 금액을 하나의 자산 가치로 변환하는 별도 도메인 필요   |
| 자동 카테고리 분류              | 분류 모델 및 분류 정책 필요                    |
| 대규모 DB Aggregation      | 실제 성능 측정 이후 판단                      |
| Cache / Pre-aggregation | 실제 성능 요구사항 확인 이후 판단                 |

---

# 6. 현재 모델의 의도적인 한계

Phase 5.1에서는 **모든 `WITHDRAWAL`을 Gross Expense로 취급한다.**

따라서 내부 계좌 이체가 현재 모델에서 다음과 같이 표현된다면:

```text
TransactionType = WITHDRAWAL
```

해당 거래는 지출 분석에 포함될 수 있다.

이는 Phase 5.1의 계산 오류가 아니다.

현재 `Transaction` 모델은 아직 다음 경제적 행위를 별도로 표현하지 않는다.

```text
자금 이동 (Transfer)
        ≠
소비 (Expense)
```

따라서 다음과 같은 임시 규칙을 도입하지 않는다.

```text
category == "이체" → 제외
```

Category 문자열을 이용해 금융 이벤트의 의미를 추론하는 것은 현재 모델의 부족한 표현력을 Analytics 계층에서 임시 보완하는 것이기 때문이다.

내부 이체의 정확한 식별 및 제외는 향후 Transaction의 금융 이벤트 의미를 확장하는 별도의 도메인 과제로 다룬다.

---

# 7. 금융 도메인 규칙

## 7.1. Gross Expense

Gross Expense는 다음과 같이 정의한다.

> 특정 기간에 발생한 `WITHDRAWAL` 거래 금액의 합계.

```text
Transaction.type == WITHDRAWAL
```

만 집계 대상이다.

`DEPOSIT`은 Gross Expense에 포함하지 않는다.

---

## 7.2. Net Expense는 계산하지 않는다

Phase 5.1에서는 다음과 같은 상쇄 관계를 정의하지 않는다.

```text
지출
- 환불
- 취소
- 부분 환급
- 더치페이 정산
= Net Expense
```

단순히 `DEPOSIT` 금액을 `WITHDRAWAL`에서 차감하는 방식도 사용하지 않는다.

어떤 입금이 어떤 지출에 대응하는지에 대한 금융 이벤트 관계가 현재 모델에 정의되어 있지 않기 때문이다.

---

# 8. 시간 정책

## 8.1. `occurredAt` 사용

분석 기간은 `Transaction.occurredAt`을 기준으로 한다.

`createdAt`은 분석 기간의 기준으로 사용하지 않는다.

| 필드           | 의미             | Analytics |
| ------------ | -------------- | --------- |
| `occurredAt` | 실제 금융 거래 발생 시각 | **사용**    |
| `createdAt`  | 시스템 기록 생성 시각   | 사용하지 않음   |

예를 들어 9월 30일에 발생하고 10월 1일에 시스템에 기록된 거래는 9월 거래로 분석한다.

---

## 8.2. 월간 기간 경계

특정 `YearMonth`의 분석 범위:

```text
[start, end)
```

예:

```text
2026-09-01 00:00:00 <= occurredAt
                              <
2026-10-01 00:00:00
```

시작은 포함하고 다음 달 시작은 포함하지 않는다.

---

## 8.3. 현재 Timezone 정책

Phase 5.1에서는 `Transaction.occurredAt`을 **`Asia/Seoul` 기준의 거래 발생 시각**으로 해석한다.

현재 `occurredAt`은 `LocalDateTime`이므로 자체적인 Timezone 정보를 가지고 있지 않다.

따라서 Phase 5.1에서는 KST라는 해석 규칙만 확정한다.

구체적으로 `ZoneId`를 직접 사용할지, 시스템 Timezone을 사용할지, 별도의 시간 추상화를 도입할지는 Decision Summary의 Pending 항목에서 결정한다.

---

# 9. 다중 통화 정책

서로 다른 Currency의 거래를 하나의 금액으로 합산하지 않는다.

```text
10,000 KRW
+
100 USD
```

는 하나의 총지출로 계산하지 않는다.

대신:

```text
KRW Report
USD Report
JPY Report
...
```

와 같이 Currency별 독립적인 분석 결과를 생성한다.

---

## 9.1. 환율 변환

Phase 5.1에서는 다음을 정의하지 않는다.

* 환율
* 환율 기준일
* 기준 통화
* 환율 제공자
* 환율 변동
* 자산 평가 기준

따라서 각 금액은 원래 Currency를 유지한다.

---

# 10. 카테고리 분석

각 Currency의 `WITHDRAWAL` 거래를 카테고리별로 그룹화한다.

예:

```text
KRW
├── 식비      50,000
├── 교통      20,000
└── 쇼핑      30,000
```

총지출:

```text
50,000 + 20,000 + 30,000
= 100,000 KRW
```

Ratio:

```text
카테고리 지출액
---------------- × 100
월 총지출
```

예:

```text
50,000 / 100,000 × 100
= 50.00%
```

---

# 11. Ratio 정책

## 11.1. 타입

`Double`을 사용하지 않고 `BigDecimal`을 사용한다.

금융 데이터의 계산 정밀도를 명시적으로 관리하기 위함이다.

---

## 11.2. 반올림

```text
Scale = 2
RoundingMode = HALF_EVEN
```

각 Category의 Ratio는 독립적으로 반올림한다.

---

## 11.3. 100% 보정 없음

독립적인 반올림으로 인해 Ratio 합계가 `100.00%`가 되지 않을 수 있다.

예:

```text
33.33%
33.33%
33.33%

= 99.99%
```

이를 임의로 100%로 보정하지 않는다.

```text
99.99% → 100.00%
```

과 같은 balancing algorithm은 적용하지 않는다.

---

## 11.4. Zero Division

분모가 0인 경우:

```text
totalExpense == 0
→ category ratio = 0%
```

로 처리한다.

현재 `Transaction` 금액 불변식상 정상적인 `WITHDRAWAL` 집계에서는 일반적으로 발생하지 않는 상황이지만, 분석 계산 자체가 0으로 인한 산술 오류를 발생시키지 않도록 정책을 명시한다.

---

## 11.5. Ratio 내부 표현

`33.33`처럼 Percentage 값을 직접 표현할지, `0.3333`처럼 Fraction을 표현할지는 구현 단계에서 결정한다.

다만 외부적으로 제공되는 의미는 **“총지출 대비 백분율”**이며, 최종 표시 정밀도는 Scale 2로 한다.

---

# 12. '미분류' 데이터 정책

현재 `Transaction` 도메인의 다음 불변식을 유지한다.

```kotlin
category.isNotBlank()
```

Analytics 구현을 위해 `Transaction.category`를 nullable로 변경하지 않는다.

---

## 12.1. Normalization 책임

외부 데이터에서 카테고리를 알 수 없는 경우 Analytics에서:

```kotlin
category ?: "미분류"
```

와 같이 처리하지 않는다.

대신:

```text
외부 데이터
    ↓
Ingestion / Normalization
    ↓
"미분류"
    ↓
Transaction
    ↓
Analytics
```

의 흐름을 사용한다.

Analytics 입장에서는 `"미분류"`도 정상적인 Category다.

---

# 13. 결과 정렬

분석 결과는 결정론적인 순서를 갖는다.

### 1차

```text
amount DESC
```

### 2차

```text
category ASC
```

예:

```text
식비     50,000
쇼핑     30,000
교통     30,000
```

동일 금액에서는 Category명으로 순서를 결정한다.

---

# 14. Empty Result

분석 대상 기간에 거래가 존재하지 않으면:

```text
[]
```

를 반환한다.

임의로:

```text
0 KRW
```

와 같은 결과를 만들지 않는다.

거래가 존재하지 않는 것과 특정 Currency의 지출이 0인 것은 서로 다른 의미이기 때문이다.

---

# 15. 분석 결과 모델

분석 결과는 영속화 Entity가 아니라 **불변 Read Model**이다.

개념적인 구조:

```text
MonthlyExpenseReport
├── yearMonth
├── currency
├── totalExpense
└── categoryExpenses
     ├── category
     ├── amount
     └── ratio
```

예상 형태:

```kotlin
data class MonthlyExpenseReport(
    val yearMonth: YearMonth,
    val currency: Currency,
    val totalExpense: Money,
    val categoryExpenses: List<CategoryExpense>
)

data class CategoryExpense(
    val category: String,
    val amount: Money,
    val ratio: BigDecimal
)
```

### 확정

* 분석 결과는 Entity가 아니다.
* 분석 결과는 영속화 대상이 아니다.
* 분석 결과는 읽기 전용 결과 모델이다.
* 기존 `Money` Value Object를 결과 모델 내부에서 사용할 수 있다.

### 미확정

* 정확한 패키지 위치
* `MonthlyExpenseReport`와 `CategoryExpense`의 정확한 모듈 위치
* Core Domain 내부에 위치할지 Application 계층의 Read Model로 위치할지
* 실제 구현 시 별도 타입을 추가할지 여부

즉, **“Read Model이라는 역할”은 확정하지만 “구체적인 클래스 배치”는 구현 단계에서 결정한다.**

---

# 16. Architecture Responsibility

Phase 5.1에서 계층의 책임은 다음과 같이 구분한다.

```text
┌─────────────────────────────────────────────┐
│                    API                      │
│                                             │
│  HTTP Request / Response                    │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│                   Core                      │
│                                             │
│  금융 분석 계산                              │
│                                             │
│  - WITHDRAWAL 필터                          │
│  - Currency 집계                            │
│  - Category 집계                            │
│  - Total Expense                            │
│  - Ratio                                    │
│  - Rounding                                 │
│  - 결과 정렬                                │
└──────────────────────┬──────────────────────┘
                       │
                       │ Port
                       ▼
┌─────────────────────────────────────────────┐
│                  Storage                    │
│                                             │
│  분석에 필요한 Transaction 데이터 공급       │
│                                             │
│  Repository / JPA / Database                │
└─────────────────────────────────────────────┘
```

핵심 원칙:

> **Storage는 데이터를 제공하고, Core는 금융적 의미를 계산한다.**

API는 금융 계산을 구현하지 않는다.

Persistence Adapter는 금융 정책을 임의로 구현하지 않는다.

---

# 17. Core 인메모리 연산

Phase 5.1에서는 분석 대상 Transaction을 Storage에서 조회하고 Core에서 금융 계산을 수행한다.

개념적으로:

```text
Storage
    ↓
List<Transaction>
    ↓
Core
    ↓
WITHDRAWAL 필터
    ↓
Currency 그룹화
    ↓
Category 그룹화
    ↓
금액 합산
    ↓
Ratio 계산
    ↓
Read Model
```

## 17.1. 선택 이유

이 결정의 핵심 이유는 단순한 테스트 편의성이 아니다.

Phase 5.1에서 우선하는 것은 **금융 계산 규칙의 응집도와 명확성**이다.

다음 정책이 SQL, Repository, Adapter, Core에 분산되는 것을 피한다.

```text
Gross Expense 정의
Ratio 계산
Rounding
Currency 규칙
Category 집계
```

금융 정책은 가능한 한 Core에서 하나의 실행 흐름으로 확인할 수 있어야 한다.

## 17.2. 성능 정책과는 구분한다

Core 인메모리 계산은 **현재 Phase 5.1의 구현 정책**이다.

이는 향후 DB Aggregation을 영구적으로 금지하는 정책이 아니다.

실제 성능 문제가 확인될 경우 다음을 검토할 수 있다.

```text
DB Projection
DB GROUP BY
분석 전용 Query
Pre-aggregation
Cache
Batch Materialization
```

다만 이러한 최적화는 실제 측정 결과를 근거로 도입한다.

---

# 18. 분석 데이터 공급 Port

Phase 5.1에서 **`LoadTransactionsForAnalysisPort`의 신설 자체를 확정하지 않는다.**

확정하는 것은 다음이다.

> Analytics Application은 금융 분석에 필요한 Transaction 데이터를 외부 Persistence에 의존하여 공급받아야 한다.

그 데이터 공급 계약을 기존 `LoadTransactionsPort`로 충족할 수 있다면 재사용할 수 있다.

반대로 분석 특화 요구사항이 기존 Port의 계약과 충돌하거나 별도의 Bulk 조회 의미를 필요로 한다면 별도 Port를 도입할 수 있다.

즉:

```text
❌ "반드시 LoadTransactionsForAnalysisPort를 만든다."

✅ "Analytics가 필요로 하는 데이터 공급 계약을 정의하고,
    기존 계약의 재사용 여부를 검토한다."
```

구체적인 Port 계약은 구현 단계에서 결정한다.

---

# 19. Port 설계 원칙

Port는 분석 기능의 개수에 따라 기계적으로 생성하지 않는다.

다음과 같은 구조를 무조건 만들지 않는다.

```text
MonthlyExpenseUseCase
MonthlyExpensePort

CategoryExpenseUseCase
CategoryExpensePort

AnnualExpenseUseCase
AnnualExpensePort
```

Port는 **외부 시스템에 요구하는 데이터 계약**을 기준으로 설계한다.

동일한 데이터 공급 계약으로 여러 분석 기능을 처리할 수 있다면 Port를 공유할 수 있다.

반대로 데이터 요구사항이 다르다면 별도의 Port를 도입한다.

---

# 20. Application Service 책임

Application 계층은 다음 흐름을 조정한다.

```text
분석 요청
    ↓
분석 대상 데이터 요청
    ↓
Core 금융 분석
    ↓
Read Model 반환
```

Application Service는 다음 금융 계산을 직접 구현하지 않는다.

```text
Ratio
Rounding
Category 합산
Currency별 금융 집계
```

이러한 금융 규칙은 Core 책임이다.

---

# 21. Core 내부 구조

다음 중 어떤 구조를 사용할지는 아직 결정하지 않는다.

```text
Application Service
      ↓
Domain Service
```

또는:

```text
Application Service
      ↓
Expense Calculator
```

또는 기타 적절한 Core 객체 구조.

확정하는 것은 **계산이 Core에 존재해야 한다는 책임 경계**이지, 특정 클래스 이름이나 패턴이 아니다.

실제 구현 시 현재 Core의 객체 구조와 응집도를 확인한 뒤 결정한다.

---

# 22. 구현 시 예상 흐름

최종적인 책임 흐름은 다음과 같다.

```text
[API]
  │
  │ 분석 요청
  ▼
[Application]
  │
  │ 분석 데이터 요청
  ▼
[Port]
  │
  ▼
[Storage Adapter]
  │
  ▼
[List<Transaction>]
  │
  ▼
[Core Financial Analytics]
  │
  ├─ WITHDRAWAL 필터
  ├─ Currency별 그룹화
  ├─ Category별 그룹화
  ├─ Total Expense 계산
  ├─ Ratio 계산
  ├─ BigDecimal / HALF_EVEN
  └─ Deterministic Sort
  │
  ▼
[Read Model]
  │
  ▼
[API Response]
```

구체적인 클래스와 Port 이름은 구현 단계에서 결정한다.

---

# 23. Persistence 구현

Storage의 책임은 분석에 필요한 거래 데이터를 제공하는 것이다.

다만 다음의 구체적인 기술 선택은 아직 결정하지 않는다.

* Spring Data JPA Query Method
* JPQL
* QueryDSL
* Native SQL
* Projection
* 별도의 Repository 메서드
* 기존 Repository 재사용 여부

현재 단계에서 확정하는 것은:

> **Persistence 계층은 금융 분석의 의미를 계산하지 않고 필요한 Transaction 데이터를 제공한다.**

---

# 24. API 설계

Phase 5.1의 금융 도메인 명세에서는 다음 API 세부사항을 확정하지 않는다.

* Endpoint URL
* HTTP Method
* Query Parameter
* Request DTO
* Response DTO
* HTTP Status
* Error Response

예를 들어:

```text
GET /api/v1/...
```

와 같은 구체적인 URL은 Web Adapter 설계 단계에서 결정한다.

단, API는 Core의 금융 계산 정책을 직접 구현하지 않는다.

---

# 25. 테스트 전략

금융 계산은 Domain/Core 테스트에서 세부적으로 검증한다.

## 25.1. Core 테스트

| 테스트 대상        | 검증 내용                      |
| ------------- | -------------------------- |
| Gross Expense | `WITHDRAWAL`만 집계           |
| Deposit 제외    | `DEPOSIT`은 지출에 포함되지 않음     |
| Currency 분리   | KRW/USD 등이 섞이지 않음          |
| Category 집계   | 동일 카테고리 금액 합산              |
| Ratio         | 총지출 대비 비중                  |
| Rounding      | Scale 2 + `HALF_EVEN`      |
| Ratio 합계      | 100% 보정하지 않음               |
| Zero Division | 분모 0 처리                    |
| 정렬            | amount DESC → category ASC |
| Empty         | 거래 없음 → 빈 결과               |
| 월 경계          | `[start, end)`             |

## 25.2. Application 테스트

Application Service가 올바른 데이터 공급 계약을 사용하고 Core 분석 결과를 반환하는지를 검증한다.

금융 계산 자체를 Application 테스트에서 중복 검증하지 않는다.

## 25.3. Persistence 테스트

Storage가 분석 데이터 공급 계약에 맞는 데이터를 반환하는지 검증한다.

특히 다음 조건을 검증한다.

```text
userId
startDateTime
endDateTime
```

단, 실제 Port 계약은 구현 단계에서 결정한다.

## 25.4. Integration 테스트

실제 Spring Context와 실제 Persistence를 사용하여 전체 흐름을 검증한다.

```text
HTTP
 ↓
Application
 ↓
Core
 ↓
Persistence
 ↓
H2
```

Integration Test는 개별 금융 규칙 테스트를 대체하지 않는다.

---

# 26. 구현 과업

## Task 1. 분석 Domain / Read Model 설계

* `MonthlyExpenseReport`
* `CategoryExpense`
* `Money`
* `Currency`

간의 관계를 확정한다.

정확한 클래스 위치는 구현 단계에서 결정한다.

---

## Task 2. Core 금융 분석 구현

다음 정책을 구현한다.

```text
WITHDRAWAL
Currency 분리
Category 집계
Total Expense
Ratio
BigDecimal
HALF_EVEN
Deterministic Sort
```

Core 내부 객체 구조는 Task 수행 과정에서 결정한다.

---

## Task 3. 데이터 공급 계약 결정

다음 질문을 결정한다.

```text
기존 LoadTransactionsPort로 충분한가?
        │
        ├── Yes → 재사용
        │
        └── No
              ↓
        분석 전용 Port 도입
```

---

## Task 4. Application Service 구현

```text
Request
→ Data Load
→ Core Analytics
→ Read Model
```

흐름을 구성한다.

---

## Task 5. Persistence Adapter 구현

분석 데이터 공급 계약을 실제 Persistence와 연결한다.

---

## Task 6. Core 테스트

금융 도메인 경계값을 검증한다.

특히:

* 월의 첫 순간
* 월의 마지막 시각
* 다음 달 1일 00:00
* 동일 금액 Category
* Ratio 반올림 경계
* 복수 Currency
* DEPOSIT 혼재
* 거래 없음

을 포함한다.

---

## Task 7. Persistence 테스트

실제 데이터 조회 계약을 검증한다.

---

## Task 8. API Adapter 구현

필요한 HTTP API를 설계하고 구현한다.

구체적인 API 계약은 구현 단계에서 결정한다.

---

## Task 9. Integration Test

최종적으로:

```text
API
 → Application
 → Port
 → Storage
 → H2
 → Transaction
 → Core Analytics
 → Read Model
 → API Response
```

의 연결을 검증한다.

---

# 27. 구현하지 말아야 할 것

Phase 5.1 구현 중 다음 요구가 발생하더라도 명세를 임의로 확장하지 않는다.

## 27.1. 내부 이체 자동 제외

```kotlin
if (transaction.category == "이체") {
    ...
}
```

구현하지 않는다.

## 27.2. 환불 자동 차감

```text
WITHDRAWAL - DEPOSIT
```

방식으로 Net Expense를 계산하지 않는다.

## 27.3. Currency 자동 환산

```text
USD → KRW
JPY → KRW
```

등을 추가하지 않는다.

## 27.4. DB 최적화 선행

성능 측정 없이 다음을 선행 도입하지 않는다.

```text
GROUP BY
SUM
Native Query
Projection
Materialized View
Cache
```

## 27.5. `Transaction.category` nullable 변경

Analytics 편의를 위해 현재 Core Domain 불변식을 깨지 않는다.

---

# 28. 향후 확장 후보

```text
Phase 5.x
├── Net Expense
│   ├── Refund
│   ├── Cancellation
│   └── Partial Refund
│
├── Transfer
│   ├── Account-to-Account Transfer
│   ├── Transfer Pair
│   └── Transfer Exclusion
│
├── Timezone
│   └── User-specific ZoneId
│
├── Multi-Currency
│   ├── FX Rate
│   └── Base Currency
│
├── Categorization
│   └── Automatic Classification
│
└── Performance
    ├── DB Aggregation
    ├── Projection
    ├── Cache
    └── Pre-aggregation
```

이 항목들은 Phase 5.1의 요구사항이 아니다.

향후 실제 요구사항이 발생하면 별도의 도메인 및 기술 설계를 수행한다.

---

# 29. Definition of Done

## Domain

* [ ] `WITHDRAWAL`만 Gross Expense로 집계된다.
* [ ] Currency가 서로 섞이지 않는다.
* [ ] Category별 금액이 정확히 집계된다.
* [ ] Ratio가 `BigDecimal` 기반으로 계산된다.
* [ ] Scale 2 / `HALF_EVEN` 정책이 적용된다.
* [ ] Ratio 100% 보정이 적용되지 않는다.
* [ ] 정렬 순서가 결정론적이다.
* [ ] 거래 없음이 빈 결과로 처리된다.
* [ ] `[start, end)` 월 경계가 정확하다.

## Architecture

* [ ] 금융 계산 책임이 Core에 있다.
* [ ] 데이터 공급 책임이 Persistence에 있다.
* [ ] API에 금융 계산 로직이 없다.
* [ ] Persistence Adapter에 금융 정책이 없다.
* [ ] 분석 데이터 공급 계약이 명확하다.
* [ ] Pending 기술 결정이 구현 과정에서 명시적으로 결정되었다.

## Tests

* [ ] Core 금융 규칙 테스트가 존재한다.
* [ ] 월 경계 테스트가 존재한다.
* [ ] Currency 분리 테스트가 존재한다.
* [ ] Ratio 반올림 테스트가 존재한다.
* [ ] Empty 결과 테스트가 존재한다.
* [ ] Persistence 조회 테스트가 존재한다.
* [ ] Integration Test가 전체 흐름을 검증한다.

## Scope

* [ ] Net Expense를 구현하지 않았다.
* [ ] Transfer 제외 로직을 구현하지 않았다.
* [ ] FX 변환을 구현하지 않았다.
* [ ] User-specific Timezone을 구현하지 않았다.
* [ ] 자동 카테고리 분류를 구현하지 않았다.
* [ ] 성능 측정 없이 DB Aggregation을 도입하지 않았다.

---

# 30. 문서 관리 원칙

이 문서에서 **금융적 의미와 Scope는 쉽게 변경하지 않는다.**

반면 기술적 구현은 실제 설계 과정에서 변경될 수 있다.

예:

```text
Repository 구현
Query 방식
Port의 구체적인 이름
Core 객체 구조
Package 구조
API DTO
DB 최적화
```

이러한 변경이 발생했다고 해서 금융 도메인 명세 자체가 틀린 것은 아니다.

중요한 기술적 결정이 아키텍처에 장기적인 영향을 미친다면 별도의 ADR로 기록한다.

예:

```text
docs/ADR.md

ADR-XXX
Financial Analytics Core In-Memory Calculation
```

그리고 해당 ADR에서:

```text
Context
Decision
Alternatives Considered
Consequences
```

를 기록한다.

`PHASE5_ANALYTICS_SPEC.md`는 **무엇을 만들고 어떤 금융 의미를 보장할 것인지**를 설명하고, ADR은 **중요한 기술 선택을 왜 했는지**를 설명한다.

---

# 31. 최종 설계 원칙

Phase 5.1은 다음 원칙을 따른다.

> **금융 정책을 코드와 SQL에 흩어놓지 않고 Core에서 명시적으로 표현한다.**

> **현재 모델이 표현할 수 없는 금융 의미를 Analytics 계층에서 임의로 추론하지 않는다.**

> **이번 단계에서 필요한 문제만 해결하고 미래의 문제를 현재 코드에 선반영하지 않는다.**

> **성능 최적화는 추측이 아니라 측정 결과를 근거로 수행한다.**

> **Read Model과 Domain Entity의 책임을 구분한다.**

> **확정된 금융 정책과 미확정된 기술 선택을 명확하게 분리한다.**

Phase 5.1의 핵심 경계는 다음과 같다.

```text
                    ┌────────────────────────┐
                    │    Phase 5.1 목표      │
                    │                        │
                    │ Monthly Gross Expense  │
                    └───────────┬────────────┘
                                │
               ┌────────────────┼────────────────┐
               ▼                ▼                ▼
           occurredAt       Currency          Category
               │                │                │
               ▼                ▼                ▼
          월간 집계          통화별 분리       비중 계산
               │                │                │
               └────────────────┼────────────────┘
                                ▼
                    Immutable Read Model
```

현재 단계에서 해결하지 않는 문제:

```text
Net Expense
Transfer
Refund
FX
User-specific Timezone
Auto Categorization
Large-scale Optimization
```

이것들은 누락된 요구사항이 아니라 **의도적으로 Deferred된 문제**다.

Phase 5.1의 목적은 모든 금융 문제를 한 번에 해결하는 것이 아니다.

목표는 **정확한 월별 Gross Expense 분석을 구현하면서, 향후 금융 분석이 확장될 수 있는 명확한 첫 번째 경계를 만드는 것**이다.
