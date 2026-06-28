package com.moneylog.storage.persistence.adapter

import com.moneylog.core.application.port.outbound.SaveTransactionPort
import com.moneylog.core.domain.Transaction
import com.moneylog.storage.persistence.entity.TransactionJpaEntity
import com.moneylog.storage.persistence.repository.TransactionJpaRepository
import org.springframework.stereotype.Component

@Component
class TransactionPersistenceAdapter(
    // 2번 무기인 JPA 레포지토리를 주입받는다.
    private val transactionJpaRepository: TransactionJpaRepository
) : SaveTransactionPort { // 💡 핵심: 무균실의 아웃바운드 포트를 여기서 구현(implements)한다!

    override fun save(transaction: Transaction): Transaction {
        // 1. [통역 단계] 무균실의 고결한 도메인 객체(Transaction)를
        //    진흙탕용 JPA 엔티티(TransactionJpaEntity)로 완벽하게 변환(Mapping)한다.
        val jpaEntity = TransactionJpaEntity(
            id = transaction.id,
            type = transaction.type,
            amount = transaction.amount.amount,              // Money 안의 BigDecimal 추출
            currencyCode = transaction.amount.currency.currencyCode, // Money 안의 통화코드 추출
            category = transaction.category,
            memo = transaction.memo,
            createdAt = transaction.createdAt,
            createdBy = transaction.createdBy,
            createdPgmId = transaction.createdPgmId
        )

        // 2. [실행 단계] 진짜 DB 기술(JPA)을 이용해 테이블에 쿼리를 쏘고 저장한다.
        val savedEntity = transactionJpaRepository.save(jpaEntity)

        // 3. [반환 단계] 저장이 끝나면 다시 무균실의 언어인 '도메인 객체'로 번역해서 코어 엔진으로 돌려준다.
        return transaction
    }
}