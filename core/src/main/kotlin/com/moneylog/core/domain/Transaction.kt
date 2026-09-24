package com.moneylog.core.domain

import java.time.LocalDateTime
import java.util.UUID

data class Transaction (
    // 식별자: 값을 지정하지 않으면 Prefix가 포함된 UUID를 자동 발급한다.
    val id: String = "TX-${UUID.randomUUID()}",
    val userId: String,
    val type: TransactionType,
    val amount: Money,
    val category: String,
    val memo: String,
    val occurredAt: LocalDateTime,

    // Auditable 인터페이스 속성들을 여기서 강제로 구현(override)하지 않는다.
    // - Persistence 모델에서 관리한다.JPA Entity 생성 시점에 주입한다.
)