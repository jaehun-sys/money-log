package com.moneylog.storage.persistence.repository

import com.moneylog.storage.persistence.entity.TransactionJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionJpaRepository : JpaRepository<TransactionJpaEntity, String> {
    // 기본 CRUD(save, findById 등)는 상속만 받아도 그레이들이 알아서 쿼리를 다 짜준다.
}