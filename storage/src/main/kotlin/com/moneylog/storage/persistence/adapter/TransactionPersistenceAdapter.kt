package com.moneylog.storage.persistence.adapter

import com.moneylog.core.application.port.inbound.LoadTransactionsQuery
import com.moneylog.core.application.port.outbound.LoadTransactionsPort
import com.moneylog.core.application.port.outbound.SaveTransactionPort
import com.moneylog.core.domain.Money
import com.moneylog.core.domain.Transaction
import com.moneylog.storage.persistence.entity.TransactionJpaEntity
import com.moneylog.storage.persistence.repository.TransactionJpaRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.Currency

@Component
class TransactionPersistenceAdapter(
    // 2번 무기인 JPA 레포지토리를 주입받는다.
    private val transactionJpaRepository: TransactionJpaRepository
) : SaveTransactionPort, LoadTransactionsPort { // 💡 핵심: Domain의 아웃바운드 포트를 여기서 구현(implements)한다!

    override fun save(transaction: Transaction): Transaction {
        // Domain 객체를 Persistence Entity로 변환한다.
        val jpaEntity = TransactionJpaEntity(
            id = transaction.id,
            userId = transaction.userId,                    // 도메인에서 넘어온 돈의 소유자
            type = transaction.type,
            amount = transaction.amount.amount,             // Money 안의 BigDecimal 추출
            currencyCode = transaction.amount.currency.currencyCode, // Money 안의 통화코드 추출
            category = transaction.category,
            memo = transaction.memo,
            occurredAt = transaction.occurredAt,            // 도메인에서 넘어온 실제 결제 발생 시각

            // 🛡️ Audit (시스템 감사) 영역: 코어 도메인이 모르는 정보를 영속성 계층 경계에서 직접 주입한다.
            createdAt = LocalDateTime.now(),       // DB 저장 시각 (또는 JPA @EntityListeners(AuditingEntityListener::class)에 위임 가능)
            createdBy = "SYSTEM_UNAUTH",           // 임시: 현재 인증 시스템이 없으므로 userId를 재사용하지 않고, 임시 인증 주체임. TODO Spring Security 적용 시 SecurityContextHolder에서 가져오도록 변경 예정.
            createdPgmId = "MONEY_LOG_API"         // 고정: API 서버를 통해 삽입되었음을 명시
        )

        // 2. DB에 실제로 INSERT 명령
        transactionJpaRepository.save(jpaEntity)

        // 3. [반환 단계] 저장이 끝나면 다시 무균실의 언어인 '도메인 객체'로 번역해서 코어 엔진으로 돌려준다.
        return transaction
    }

    override fun load(query: LoadTransactionsQuery): List<Transaction> {
        // 💡 DB 쿼리를 위한 시간 경계 변환 책임을 Adapter가 진다.
        val startDateTime = query.startDate.atStartOfDay()
        val endDateTime = query.endDate.plusDays(1).atStartOfDay()

        // 1. DB에서 조건에 맞는 Entity 목록 조회 (Half-Open Interval)
        val entities = transactionJpaRepository.findTransactions(
            userId = query.userId,
            startDate = startDateTime,
            endDate = endDateTime
        )

        // 2. 조회된 Entity들을 무균실의 순수 Domain 객체로 복원하여 반환
        return entities.map { toDomain(it) }
    }

    // 💡 Persistence Entity를 Domain 객체로 변환한다.
    private fun toDomain(entity: TransactionJpaEntity): Transaction {
        return Transaction(
            id = entity.id,
            userId = entity.userId,
            type = entity.type,
            amount = Money.of(entity.amount, Currency.getInstance(entity.currencyCode)), // String 타입의 currencyCode를 다시 Currency 객체로 복원하여 주입
            category = entity.category,
            memo = entity.memo,
            occurredAt = entity.occurredAt
        )
    }
}
