package com.moneylog.core.application.port.inbound

import com.moneylog.core.domain.Transaction

/**
 * [인바운드 포트] 거래 내역 조회 유스케이스
 */
interface LoadTransactionsUseCase {

    /**
     * 특정 사용자의 기간별 거래 내역을 조회한다.
     */
    fun load(query: LoadTransactionsQuery): List<Transaction>
}