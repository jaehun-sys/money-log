package com.moneylog.core.application.port.inbound

import com.moneylog.core.domain.Transaction
import com.moneylog.core.domain.TransactionType
import java.time.LocalDateTime

/**
 * [인바운드 포트] 가계부 거래 내역 등록 유즈케이스
 * - 외부 계층(Web, CLI, Message Queue 등)에서 코어 도메인으로 들어오기 위해 반드시 거쳐야 하는 '정문' 인터페이스.
 * - 이 인터페이스의 실제 구현체(Service)는 애플리케이션 계층 내부에서 작성된다.
 */
interface RecordTransactionUseCase {

    /**
     * @param command 유즈케이스 실행에 필요한 모든 데이터가 담긴 검증된 명령서
     * @return Transaction : 비즈니스 로직(검증, 조립)을 통과하고 성공적으로 생성된 도메인 엔티티
     */
    fun record(command: RecordTransactionCommand): Transaction
}

/**
 * [Command 객체] 유즈케이스 실행을 위한 전용 입력 DTO (자가 검증 커맨드)
 * - Spring Web의 Request DTO(JSON 매핑용 객체)가 코어 도메인으로 침투하는 것을 막는 방패막이.
 * - 생성되는 즉시 스스로 데이터의 유효성(Syntax)을 검증하여, 도메인 로직에 쓰레기 값이 도달하는 것을 원천 차단한다.
 */
data class RecordTransactionCommand(
    var type: TransactionType,
    val amount: Double,             // 화면에서 넘어오는 값 (나중에 Money.of()로 안전하게 변환될 예정)
    val currencyCode: String,       // "KRW", "USD" 등
    val category: String,
    val memo: String,
    val timestamp: LocalDateTime,   // 사용자가 결제한 시간
    val userId: String,             // 누가 등록했는지 (Auditable용)
) {
    // 💡 Command 객체가 생성되는 즉시 실행되는 초기화 블록
    init {
        // [사전 검증] 비즈니스 로직을 타기 전,쓰레기 값이 들어오면 유즈케이스(서비스) 로직을 타기도 전에 여기서 모가지(Exception)를 비틀어버린다.
        require(amount > 0) { "결제 금액은 0보다 커야 한다. 입력값: $amount" }
        require(currencyCode.isNotBlank()) { "통화 코드가 비어 있다." }
        require(category.isNotBlank()) { "카테고리는 필수 입력값이다." }
        require(userId.isNotBlank()) { "사용자 식별자가 누락되었다." }
        require(memo.length <= 255) { "메모는 255자를 초과할 수 없다. 현재 길이: ${memo.length}" }
    }
}