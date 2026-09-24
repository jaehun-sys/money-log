package com.moneylog.core.application.port.inbound

import com.moneylog.core.domain.Transaction
import com.moneylog.core.domain.TransactionType
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * [인바운드 포트] 거래 내역 등록 유스케이스
 *
 * 외부 진입점(Web, Batch, Message Queue 등)에서 코어 도메인으로 진입하기 위한 단일 창구.
 * 포트의 입력 스펙은 프로토콜 종속적인 DTO 대신 불변 커맨드(Command)를 사용한다.
 */
interface RecordTransactionUseCase {

    /**
     * @param command 유스케이스 실행에 필요한 불변조건이 검증된 명령 객체
     * @return Transaction 영속화 및 도메인 규칙 처리가 완료된 도메인 모델
     */
    fun record(command: RecordTransactionCommand): Transaction
}

/**
 * [Command 객체] 유스케이스 실행 명세서 (Self-Validating Command)
 *
 * HTTP/JSON 등의 외부 프로토콜과 분리된 순수 애플리케이션 입력 모델이다.
 *
 * 모든 필드는 생성 시점에 확정되며,
 * 유스케이스 실행에 필요한 입력 불변조건을 스스로 검증한다.
 */
data class RecordTransactionCommand(
    val type: TransactionType,
    val amount: BigDecimal,
    val currencyCode: String,
    val category: String,
    val memo: String,
    val occurredAt: LocalDateTime,
    val userId: String,
) {
    init {
        // [입력 불변조건 검증] 유스케이스 실행 전 결함이 있는 데이터는 생성 단계에서 즉시 차단한다.
        require(amount > BigDecimal.ZERO) {
            "결제 금액은 0보다 커야 합니다. 입력값: $amount"
        }

        require(currencyCode.isNotBlank()) {
            "통화 코드는 필수입니다."
        }

        require(category.isNotBlank()) {
            "카테고리는 필수입니다."
        }

        require(userId.isNotBlank()) {
            "사용자 식별자는 필수입니다."
        }

        require(memo.length <= 255) {
            "메모는 255자를 초과할 수 없습니다. 현재 길이: ${memo.length}"
        }
    }
}