package com.moneylog.core.application.port.outbound

import com.moneylog.core.application.port.inbound.LoadTransactionsQuery
import com.moneylog.core.domain.Transaction

/**
 * [아웃바운드 포트] 거래 내역 조회 인터페이스
 *
 * Core는 실제 데이터가 DB에서 오는지,
 * 다른 API에서 오는지 알지 못한다.
 *
 * 구체적인 조회 기술은 외부 Adapter가 구현한다.
 */
interface LoadTransactionsPort {

    /**
     * 조회 조건에 해당하는 거래 내역을 반환한다.
     */
    fun load(query: LoadTransactionsQuery): List<Transaction>
}