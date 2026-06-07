package com.moneylog.core.domain

import java.time.LocalDateTime
import java.util.UUID

data class Transaction (
    // 1. 식별자: 생성 시 값을 안 넣으면 무작위 UUID(Prefix 포함) 자동 발급!
    val id: String = "TX-${UUID.randomUUID()}",
    val type: TransactionType,
    val amount: Money,
    val category: String,
    val memo: String,

    // Auditable 인터페이스 속성들을 여기서 강제로 구현(override)하게 만든다.
    override val createdAt: LocalDateTime,
    override val createdBy: String,
    override val createdPgmId: String,
    // 기본값(= null)
    override val updatedAt: LocalDateTime? = null,
    override val updatedBy: String? = null,
    override val updatedPgmId: String? = null
) : Auditable {
}