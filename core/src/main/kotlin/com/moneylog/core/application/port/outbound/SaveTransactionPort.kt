package com.moneylog.core.application.port.outbound

import com.moneylog.core.domain.Transaction

/**
 * [아웃바운드 포트] 가계부 거래 내역 저장 인터페이스
 * - 코어 애플리케이션이 외부 스토리지(DB)에 데이터를 저장하기 위해 호출하는 '뒷문'
 * - 중요: 이 인터페이스에는 JPA, MySQL, Mongo 같은 구체적인 인프라 기술의 냄새가 1도 나서는 안 된다.
 */
interface SaveTransactionPort {

    /**
     * 완성된 도메인 엔티티를 영속화(저장)한다.
     * * @param transaction 비즈니스 검증과 조립이 모두 끝난 순수 도메인 객체
     * @return DB에 성공적으로 저장되었음이 '보장된' 도메인 객체
     */
    fun save(transaction: Transaction): Transaction
}