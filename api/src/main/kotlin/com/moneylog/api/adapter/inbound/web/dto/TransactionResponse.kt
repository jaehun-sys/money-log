package com.moneylog.api.adapter.inbound.web.dto

import com.moneylog.core.domain.Transaction
import com.moneylog.core.domain.TransactionType
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * [Web Response DTO]
 *
 * Core Domain을 HTTP 응답에 직접 노출하지 않기 위한
 * Outbound Web Adapter 전용 응답 모델.
 */
data class TransactionResponse(
    val id: String,
    val userId: String,
    val type: TransactionType,
    val amount: BigDecimal,
    val currency: String,
    val category: String,
    val memo: String,
    val occurredAt: LocalDateTime,
) {
    companion object {
        fun from(transaction: Transaction): TransactionResponse {
            return TransactionResponse(
                id = transaction.id,
                userId = transaction.userId,
                type = transaction.type,
                amount = transaction.amount.amount,
                currency = transaction.amount.currency.currencyCode,
                category = transaction.category,
                memo = transaction.memo,
                occurredAt = transaction.occurredAt
            )
        }
    }
}