package com.moneylog.core.application.service

import com.moneylog.core.application.port.inbound.RecordTransactionCommand
import com.moneylog.core.application.port.inbound.RecordTransactionUseCase
import com.moneylog.core.application.port.outbound.SaveTransactionPort
import com.moneylog.core.domain.Money
import com.moneylog.core.domain.Transaction
import java.util.Currency

/**
 * [애플리케이션 서비스] 가계부 등록 유즈케이스 구현체
 * - 인바운드 포트(UseCase)를 구현하고, 아웃바운드 포트(Port)를 호출한다.
 * - 주의: 스프링의 @Service, @Transactional 같은 기술적 어노테이션은 코어 격리를 위해 일절 사용하지 않는다.
 */
class RecordTransactionService(
    private val saveTransactionPort: SaveTransactionPort // 뒷문(Outbound Port)을 주입받는다.
) : RecordTransactionUseCase { // 정문(Inbound Port)의 계약을 이행한다.

    override fun record(command: RecordTransactionCommand): Transaction {

        // 1. 비즈니스 룰: 날것의 데이터를 안전한 Money 객체로 변환 (소수점 방어)
        val money = Money.of(
            amount = command.amount,
            currency = Currency.getInstance(command.currencyCode)
        )

        // 2. 비즈니스 룰: Command의 데이터를 Transaction 엔티티로 조립
        val transaction = Transaction(
            // id는 우리가 어제 Transaction.kt에 설정한 "TX-UUID" 룰에 의해 자동 생성됨 (생략)
            type = command.type,
            amount = money,
            category = command.category,
            memo = command.memo,
            createdAt = command.timestamp,
            createdBy = command.userId,
            createdPgmId = "MONEY_LOG_API" // (일단 API 서버 식별자로 고정. 향후 확장 가능)
        )

        // 3. 비즈니스 룰: 조립된 도메인 객체를 뒷문을 통해 영속화하고 그 결과를 반환
        return saveTransactionPort.save(transaction)
    }
}