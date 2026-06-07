package com.moneylog.core.domain

enum class TransactionType(val description: String) {
    DEPOSIT("입금"),
    WITHDRAWAL("출금"),
    TRANSFER("이체")
}