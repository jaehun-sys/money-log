# money-log AI 협업 운영 규약

> **문서 버전:** v0.1  
> **현재 운영 모드:** v0 — Antigravity 단독 코딩 에이전트  
> **향후 운영 모드:** v1 — Claude Code + Antigravity  
> **저장 경로:** `docs/workflows/AI_COLLABORATION.md`  
> **상태:** APPROVED

## 1. 목적과 원칙

money-log는 단순 가계부가 아닌 **금융 데이터 표준화 및 분석 엔진**이다. AI 협업의 목표는 금융 도메인의 정확성, 아키텍처 일관성, 구현 검증 가능성을 유지하면서 **사용자의 반복적인 비교·중재 부담을 줄이는 것**이다.

핵심 원칙:

1. 사용자는 **Product Owner이자 최종 설계·Merge 승인자**다. AI는 독립적으로 최종 승인하지 않는다.
2. GPT는 **Lead Architect / Review Integrator**, Gemini Pro는 **Independent Challenger**다. 두 AI에게 매번 전체 설계안을 각각 작성시키지 않는다.
3. Gemini의 지적은 구체적 반례와 근거로 평가하며, GPT가 자기 설계를 방어한다는 이유만으로 배척하지 않는다.
4. 코드 에이전트는 **승인된 SPEC/ADR**에 따라 구현한다. 구현 중 금융 규칙 또는 아키텍처 충돌을 발견하면 임의 변경하지 않고 상위 설계 단계로 돌린다.
5. 읽지 않은 코드를 읽었다고, 실행하지 않은 테스트를 통과했다고 주장하지 않는다.
6. **간단한 작업에 모든 AI를 동원하지 않는다.** 중요도에 따라 절차를 간소화한다.
7. 이 규약은 협업 방식을 정한다. 금융 정책을 규정하는 `PRODUCT.md`, 승인된 SPEC/ADR을 임의로 덮어쓰지 않는다.

## 2. 근거 자료와 권한

| 자료 | 의미 |
|---|---|
| `docs/PRODUCT.md` | 제품 철학, Core 독립성 등 프로젝트 전체 원칙 |
| `docs/specs/` | 기능별 승인된 요구사항·금융 계산 정책 |
| `docs/adr/` | 중요한 설계 결정 및 근거 |
| 실제 저장소 소스·테스트 | 현재 구현 상태를 보여 주는 증거. 기존 정책보다 자동 우선하지 않음 |
| `docs/workflows/AI_COLLABORATION.md` | AI 협업 운영 규약 |
| AI의 Proposal/Review | 검토 자료. **승인 전에는 공식 설계가 아님** |

정보는 반드시 **확정된 정책 / 실제 코드·실행으로 확인된 사실 / 추정 / 제안**으로 구분한다. 문서와 실제 코드가 충돌하면 충돌을 보고하고 어느 쪽을 수정할지 승인받는다.

## 3. AI별 역할

| 구성원 | v0 (현재) | v1 (Claude 도입 후) |
|---|---|---|
| **사용자** | 요구사항, 핵심 결정 승인, Merge 승인 | 동일 |
| **GPT Plus** | Lead Architect: 설계, 대안·리스크 평가, Gemini 지적 통합, 최종 결정 보고서 및 구현 계약 작성 | 동일 |
| **Gemini Pro** | Independent Challenger: 반례·불변식 위반·테스트 누락 탐색 | 동일 |
| **Antigravity** | **단독 구현 + 테스트 + 자체 Diff 점검** | **독립 코드 리뷰 + 검증** (초기 배정) |
| **Claude Code** | **미도입, 사용하지 않음** | **주 구현 담당** (초기 배정) |
| **Codex** | 사용하지 않음 | 사용하지 않음 |

GPT·Gemini는 대화형 도구로 운영하며, 서로의 대화 내용이 자동 공유된다고 전제하지 않는다. GPT는 이 공유 계정에서 Codex 코딩 에이전트 역할을 맡지 않는다. v1의 에이전트별 역할은 실제 성능 검증 후 조정할 수 있다.

## 4. 작업 등급

| 등급 | 예시 | 필수 절차 |
|---|---|---|
| **L1 — Routine** | 오탈자, 명확한 기계적 수정, 작은 테스트 보완 | 코딩 에이전트 → 테스트/자체 점검 → 사용자 확인 |
| **L2 — Feature** | 승인된 금융 규칙 내 API 추가, 일반 기능 확장, 제한된 리팩토링 | GPT의 간단한 Brief 또는 기존 SPEC → 구현 → 검증 보고 → 사용자 승인 |
| **L3 — Architecture / Financial** | Money/Transaction 불변식, 금융 계산 정책, 도메인 모델, 핵심 아키텍처, 데이터 정합성 정책 변경 | GPT 설계 → Gemini 독립 검증 → GPT 통합 → 사용자 결정 → 구현 → 검증 → Merge 승인 |

L1/L2 작업에서 핵심 금융 의미나 아키텍처 변경이 발견되면 **L3로 상향**한다. 애매하다면 높은 등급을 택한다.

## 5. L3 설계 협업 프로세스

### Gate 0 — Design Brief 작성

GPT는 **하나의 공통 요청서**를 만든다.

- Task ID, 목표 및 요구사항
- 저장소·커밋·소스 확인 여부
- 관련 `PRODUCT.md`/SPEC/ADR 및 확정 정책
- 포함/제외 범위
- 완료 조건(Acceptance Criteria)
- 확인되지 않은 가정과 미해결 사항

최신 소스를 확인하지 않았다면 해당 부분을 반드시 **UNVERIFIED**로 표시한다.

### Gate 1 — GPT 설계

GPT가 권장안과 의미 있는 대안, 책임 분리, 의존성 방향, 트레이드오프, 실패 시나리오, 테스트 전략을 제안한다. **이 단계에서 코드를 구현하지 않는다.**

### Gate 2 — Gemini 독립 검증

Gemini는 Brief, 관련 SPEC/ADR, GPT 설계를 근거로 검토한다. 전체 설계를 처음부터 다시 쓰는 것이 기본 역할은 아니다. 중요한 금융 정책에서는 **설계안을 보기 전 Brief만으로 위험 목록을 먼저 작성**해 앵커링 편향을 줄일 수 있다.

지적마다 다음을 요구한다.

`Issue ID / Severity(Critical·Major·Minor·Suggestion) / 근거 또는 가정 / 구체적인 반례 / 영향 / 최소 수정안 / 검증 방법`

억지 반론, 근거 없는 성능 우려, 단순 취향 차이를 결함처럼 표현하지 않는다.

### Gate 3 — GPT 반론 처리 및 통합

GPT는 Gemini 지적을 하나씩 검증해 다음으로 분류한다.

- **Accepted:** 타당하여 설계에 반영
- **Rejected:** 근거가 부족하거나 기존 규칙과 충돌함. 반박 근거 기록
- **Deferred:** 타당하지만 이번 구현 범위 밖. 후속 과제로 기록
- **Unresolved:** 코드·테스트 증거나 사용자 결정 필요

Gemini의 **원래 지적과 GPT 처리 근거를 모두 보존**한다. 금융 정확성에 영향을 주는 Critical 지적은 근거 없이 Resolved 처리하지 않는다. 검증이 필요하면 코드 에이전트에 **읽기 전용 조사·검증**을 요청한다.

### Gate 4 — 사용자 결정 게이트

GPT는 **두 개의 원본 설계안이 아니라 하나의 통합 결정 보고서**를 제공한다.

1. 최종 권장안과 채택 이유
2. 주요 대안과 미채택 이유
3. Gemini 지적 및 처리 결과 (특히 Critical/Major)
4. 미해결 위험과 검증 한계
5. **사용자가 실제 승인해야 하는 결정만**

사용자 승인 전에는 SPEC/ADR을 공식 확정 상태로 변경하지 않는다. 금융 정확성에 중대한 미해결 문제가 있으면 구현 승인을 중단하거나 범위를 명시적으로 재설정한다.

## 6. 코딩 에이전트에 전달할 Implementation Contract

승인된 설계에 대해 GPT가 아래 항목을 하나의 구현 계약으로 정리한다.

```text
Task ID / Title:
Approved SPEC / ADR:
기준 브랜치 및 확인된 Commit (알고 있는 경우):
목표 / Acceptance Criteria:
구현 범위(In Scope):
제외 범위(Out of Scope) 및 금지사항:
수정 대상 모듈/파일 (실제 확인한 범위에서만):
유지해야 할 금융 불변식과 경계 사례:
필수 테스트 / 검증 명령:
허용되는 문서 변경:
구현 중 중단·재검토 조건:
결과 보고 양식:
```

**중단·재검토 조건:** 실제 코드와 승인 설계의 충돌, 금융 의미 불명확, 승인 범위 밖의 수정 필요, 중대한 테스트 실패, 데이터 손실 또는 보안 위험. 에이전트는 문제를 숨기거나 독자적으로 도메인 규칙을 변경해서는 안 된다.

## 7. v0 — Antigravity 단독 운영 (현재 적용)

> **Claude Code가 아직 도입되지 않았으므로 Antigravity 한 개만 코딩 에이전트로 사용한다.**

1. 사용자·GPT가 작업 등급과 요구사항을 결정한다.
2. L3라면 GPT 설계 → Gemini 검증 → GPT 통합 → 사용자 승인 단계를 거친다.
3. GPT가 승인된 Implementation Contract를 작성한다.
4. **Antigravity가 단독으로** 소스를 확인하고 구현·테스트 작성·실행·자체 Diff 리뷰를 수행한다.
5. Antigravity는 변경 파일, 실제 실행한 테스트, 실패·미실행 항목, 남은 위험을 담은 **Delivery & Self-Verification Report**를 제출한다.
6. 사용자가 최종 결과를 확인하고 Merge를 승인한다. 필요하면 GPT/Gemini가 Diff·테스트 로그를 추가로 검토할 수 있다.

### v0의 품질 한계

Antigravity가 구현과 검증을 모두 수행하기 때문에 **독립 코드 리뷰가 아니다.** 검증 상태는 반드시 `SELF-VERIFIED`로 표기하며 `INDEPENDENTLY REVIEWED`라고 쓰지 않는다. 다른 대화형 AI의 문서 리뷰도 실제 코드를 독립 실행한 검증과 구분한다.

### v0 안전장치

- 작업 단위별 Git 브랜치 사용
- 승인 없는 `reset --hard`, `clean`, 강제 Push, DB 파괴 작업 금지
- 자동 Merge·배포 금지
- 비밀키·자격증명 커밋 금지
- 테스트 통과 주장 시 실제 실행 명령과 결과 제시
- 변경 범위 이탈 또는 금융 규칙 변경 시 작업 중단 후 보고

## 8. v1 — Claude Code 도입 후 (계획, 미적용)

### 전환 조건

Claude Code 설치·인증·저장소 접근이 완료되고, 작은 파일 변경 및 테스트 보고 **파일럿**을 통과한 뒤 사용자가 **v1 활성화를 명시적으로 승인**한다. 단순히 설치했다고 자동으로 v1으로 전환하지 않는다.

### 운영 절차

1. GPT/Gemini 설계 검증과 사용자 승인 방식은 v0과 동일하다.
2. **Claude Code**가 승인된 Implementation Contract를 기준으로 구현하고 변경 Diff·테스트 결과를 제출한다.
3. **Antigravity**는 승인된 SPEC/ADR과 실제 Diff를 기준으로 독립 리뷰한다. Claude의 자체 평가를 검증 근거로 그대로 수용하지 않는다.
4. Antigravity는 테스트·금융 불변식·변경 범위·회귀 가능성을 검토하고 `PASS / PASS WITH CAVEATS / FAIL` 및 근거를 보고한다.
5. Claude가 수정 가능한 지적을 반영하고, 수정 부분을 재검토한다. 도메인/설계 논쟁이면 코딩 에이전트끼리 합의하지 않고 설계 게이트로 되돌린다.
6. 사용자는 **통합 검증 보고서 한 건**을 확인하고 Merge 여부를 결정한다.

### v1 작업 격리

- 서로 다른 Git Worktree 또는 분리된 브랜치 권장
- Antigravity의 리뷰는 기본적으로 **읽기 전용**
- 동일 작업 트리에 동시 쓰기 금지
- 리뷰어가 직접 수정 역할을 맡으면 역할 변경을 기록하고, 해당 변경에 대해 새 검증을 요청
- Claude 구현 / Antigravity 검증 배정은 파일럿 후 성과에 따라 조정 가능

## 9. 테스트·검증·Merge 기준

실제 실행 결과를 아래처럼 구분한다.

| 상태 | 의미 |
|---|---|
| **PASS** | 실제 명령을 실행하여 통과. 명령과 결과 기재 |
| **FAIL** | 실행했으나 실패. 오류와 영향 기재 |
| **NOT RUN** | 실행하지 않음. 이유 기재 |
| **N/A** | 해당 작업에 적용되지 않음. 이유 기재 |

검증 시 요구사항 충족, 금융 불변식, Core와 Persistence 책임 경계, 반례 테스트, 불필요한 파일 변경, 보안정보 유출, 파괴적 동작, 문서 동기화를 확인한다.

**빌드 성공만으로 기능 승인이 완료되지 않는다.** 최종 Merge는 항상 사용자가 승인한다.

## 10. 표준 보고서 양식

### 10.1 Gemini Challenger Review

```text
Task ID:
검토한 설계 버전 및 참고 SPEC/ADR:
실제 확인한 자료와 한계:
Findings:
  - ID / Severity / Evidence / Counterexample / Impact / Minimal Fix / Verification
문제없다고 판단한 영역:
남은 불확실성:
```

### 10.2 GPT Architecture Decision Summary

```text
Task ID:
최종 권장 설계와 근거:
주요 대안 및 미채택 이유:
Gemini Findings: ID → Accepted | Rejected | Deferred | Unresolved (각각 근거)
남은 Critical/Major 이슈:
사용자 승인 필요 항목:
승인 후 SPEC/ADR 반영 위치:
```

### 10.3 Coding Agent Delivery & Verification Report

```text
Task ID / 브랜치 / 확인된 커밋:
변경 파일과 목적:
Acceptance Criteria 충족 여부:
실제로 실행한 명령과 PASS/FAIL/NOT RUN/N/A 결과:
Diff 리뷰 지적 및 처리 결과:
승인 설계와 다른 변경사항:
미해결 문제 및 회귀 위험:
검증 형태: SELF-VERIFIED(v0) | INDEPENDENTLY REVIEWED(v1)
권고: Ready for owner review | Needs work | Blocked
```

## 11. 문서 관리 정책

- 사소한 모든 작업에 별도 Proposal 아카이브를 강제하지 않는다.
- 중요한 L3 작업에는 Brief, Gemini 지적, 통합 결정, 검증 근거를 경량 작업 기록 또는 PR에 남긴다.
- **승인된 결정만** `docs/specs/` 또는 `docs/adr/`에 공식 반영한다.
- 초안은 `PROPOSED`, 승인된 내용은 `APPROVED`, 보류된 후속 과제는 `DEFERRED`로 명시한다.
- 운영 과정에서 불필요한 부담이나 검증 누락이 발견되면 규약을 개정하되 사용자 승인을 받는다.

## 12. 단계별 도입 계획

**v0 시범 운영:** Phase 5.1 Core 설계와 후속 Antigravity 구현에 적용한다. 사용자 검토 시간, 누락된 요구사항, 테스트 품질, 전달 과정의 번거로움을 관찰한다.

**v1 전환:** Claude Code 도입 후 파일럿으로 안전한 저장소 접근, 변경 범위 준수, 실제 테스트 결과, 리뷰 품질을 확인하고 사용자 승인 후 전환한다.

**현재 제외:** 자동 다중 에이전트 오케스트레이션, 사용자 승인 없는 Merge/배포, AI 간 대화 자동 동기화, 모든 작업에서 네 AI의 의무적 참여.

## 13. 변경 이력

| 버전 | 상태 | 내용 |
|---|---|---|
| **v0.1** | APPROVED | GPT 주 설계 + Gemini 반증 검토; Antigravity 단독 구현·자체 검증 (v0 운영 규약 승인) |
| **v1.0** | 향후 계획 | Claude Code 구현 + Antigravity 독립 리뷰; 파일럿 및 사용자 승인 후 적용 |
