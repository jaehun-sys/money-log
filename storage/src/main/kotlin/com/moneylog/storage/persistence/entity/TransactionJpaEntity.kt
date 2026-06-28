package com.moneylog.storage.persistence.entity

import com.moneylog.core.domain.TransactionType
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "transactions")
class TransactionJpaEntity (
    @Id
    @Column(name = "transaction_id", length = 50)
    val id: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val type: TransactionType,

    // 💡 무균실의 Money 객체는 DB가 모르기 때문에, 금액(BigDecimal)과 통화코드(String)로 분리해 진흙탕 규격에 맞춘다.
    @Column(nullable = false, precision = 19, scale = 4)
    val amount: BigDecimal,

    @Column(nullable = false, length = 3)
    val currencyCode: String,

    @Column(nullable = false, length = 50)
    val category: String,

    @Column(length = 255)
    val memo: String,

    @Column(nullable = false)
    val createdAt: LocalDateTime,

    @Column(nullable = false, length = 50)
    val createdBy: String,

    @Column(nullable = false, length = 50)
    val createdPgmId: String
)