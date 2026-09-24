package com.moneylog.core.application.service

import com.moneylog.core.application.port.inbound.LoadTransactionsQuery
import com.moneylog.core.application.port.inbound.LoadTransactionsUseCase
import com.moneylog.core.application.port.outbound.LoadTransactionsPort
import com.moneylog.core.domain.Transaction

/**
 * [애플리케이션 서비스] 거래 내역 조회 유스케이스 구현체
 *
 * Core는 Spring/JPA 등의 기술에 의존하지 않는다.
 */
class LoadTransactionsService(
    private val loadTransactionsPort: LoadTransactionsPort
) : LoadTransactionsUseCase {

    override fun load(query: LoadTransactionsQuery): List<Transaction> {
        return loadTransactionsPort.load(query)
    }
}