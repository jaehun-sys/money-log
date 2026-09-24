package com.moneylog.storage.persistence.repository

import com.moneylog.storage.persistence.entity.TransactionJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TransactionJpaRepository : JpaRepository<TransactionJpaEntity, String> {
    // 기본 CRUD(save, findById 등)는 상속만 받아도 그레이들이 알아서 쿼리를 다 짜준다.

    // 💡 Half-Open Interval (startDate <= occurredAt < endDate) 정책 적용
    @Query("""
        SELECT t FROM TransactionJpaEntity t 
        WHERE t.userId = :userId 
          AND t.occurredAt >= :startDate 
          AND t.occurredAt < :endDate
    """)
    fun findTransactions(
        @Param("userId") userId: String,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<TransactionJpaEntity>
}
