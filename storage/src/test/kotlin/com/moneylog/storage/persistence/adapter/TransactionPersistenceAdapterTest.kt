package com.moneylog.storage.persistence.adapter

import com.moneylog.core.domain.Money
import com.moneylog.core.domain.Transaction
import com.moneylog.core.domain.TransactionType
import com.moneylog.storage.persistence.repository.TransactionJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Currency

@DataJpaTest // 💡 핵심: 무거운 웹 환경 다 빼고 DB 테스트용 스프링 환경만 초고속으로 띄운다.
@Import(TransactionPersistenceAdapter::class) // 💡 어댑터도 스프링 빈으로 강제 등록해서 데려온다.
// 💡 길 잃은 스프링에게 엔티티와 레포지토리의 정확한 위치를 꽂아준다!
@EntityScan(basePackages = ["com.moneylog.storage.persistence.entity"])
@EnableJpaRepositories(basePackages = ["com.moneylog.storage.persistence.repository"])
class TransactionPersistenceAdapterTest {

    @Autowired
    private lateinit var adapter: TransactionPersistenceAdapter

    @Autowired
    private lateinit var repository: TransactionJpaRepository

    @Test
    @DisplayName("무균실의 도메인 객체가 진흙탕(DB)의 JPA 엔티티로 완벽하게 번역되어 저장된다")
    fun saveTransactionTest() {
        // given: 무균실의 순수 도메인 객체를 생성한다 (Transaction 생성자에 맞게 필드는 조절)
        val domainTransaction = Transaction(
            id = "TX-99999",
            type = TransactionType.WITHDRAWAL,
            // 💡 Money 객체 생성 방식은 코어 모듈 구현체에 맞춘다.
            // 예: Money.won(4500) 또는 Money(BigDecimal("4500"), Currency.getInstance("KRW"))
            amount = Money.of(BigDecimal("4500.00"), Currency.getInstance("KRW")),
            category = "식비",
            memo = "국밥 특",
            createdAt = LocalDateTime.now(),
            createdBy = "USER-001",
            createdPgmId = "TEST_PGM"
            // (updatedAt 등 Auditable 필드가 더 있다면 빈칸 없이 채워줄 것)
        )

        // when: 어댑터를 호출하여 데이터베이스 저장을 지시한다 (이때 Insert 쿼리가 발생해야 함)
        adapter.save(domainTransaction)

        // then: JPA 레포지토리를 통해 진짜 H2 DB에서 데이터를 다시 긁어와서 값이 오염되지 않았는지 대조한다.
        val savedEntity = repository.findById("TX-99999").orElseThrow {
            IllegalArgumentException("DB에 데이터가 저장되지 않았습니다!")
        }

        // BigDecimal은 소수점 스케일 때문에 compareTo로 비교.
        assertThat(savedEntity.id).isEqualTo("TX-99999")
        assertThat(savedEntity.amount.compareTo(BigDecimal("4500.00"))).isEqualTo(0)
        assertThat(savedEntity.currencyCode).isEqualTo("KRW")
        assertThat(savedEntity.category).isEqualTo("식비")
        assertThat(savedEntity.memo).isEqualTo("국밥 특")
    }
}