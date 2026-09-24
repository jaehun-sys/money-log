package com.moneylog.core.application.port.inbound

import java.time.LocalDate

/**
 * [Query 객체] 거래 내역 조회 조건
 *
 * 특정 사용자의 거래 내역을 기간 기준으로 조회하기 위한
 * 애플리케이션 입력 모델이다.
 *
 * 날짜 자체를 조회 SQL이나 JPA 조건으로 표현하지 않는다.
 * 실제 데이터 조회 방식은 Outbound Adapter가 책임진다.
 */
data class LoadTransactionsQuery(
    val userId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
) {
    init {
        require(userId.isNotBlank()) {
            "사용자 식별자는 필수입니다."
        }

        require(startDate <= endDate) {
            "조회 시작일은 종료일보다 늦을 수 없습니다."
        }
    }
}