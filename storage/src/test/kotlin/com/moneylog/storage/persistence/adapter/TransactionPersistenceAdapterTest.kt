package com.moneylog.storage.persistence.adapter

import com.moneylog.core.application.port.inbound.LoadTransactionsQuery
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
import java.time.LocalDate
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
    @DisplayName("도메인 객체를 JPA Entity로 변환하여 DB에 저장한다")
    fun saveTransactionTest() {
        // given: 순수 도메인 객체를 생성한다 (Transaction 생성자에 맞게 필드는 조절)
        val domainTransaction = Transaction(
            id = "TX-99999",
            userId = "USER-001",
            type = TransactionType.WITHDRAWAL,
            // 💡 Money 객체 생성 방식은 코어 모듈 구현체에 맞춘다.
            // 예: Money.won(4500) 또는 Money(BigDecimal("4500"), Currency.getInstance("KRW"))
            amount = Money.of(BigDecimal("4500.00"), Currency.getInstance("KRW")),
            category = "식비",
            memo = "국밥 특",
            occurredAt = LocalDateTime.of(2026, 9, 15, 12, 30),
        )

        // when: 어댑터를 호출하여 데이터베이스 저장을 지시한다 (이때 Insert 쿼리가 발생해야 함)
        adapter.save(domainTransaction)

        // then: JPA 레포지토리를 통해 진짜 H2 DB에서 데이터를 다시 긁어와서 값이 오염되지 않았는지 대조한다.
        val savedEntity = repository.findById("TX-99999").orElseThrow {
            IllegalArgumentException("DB에 데이터가 저장되지 않았습니다!")
        }

        // BigDecimal은 소수점 스케일 때문에 compareTo로 비교.
        assertThat(savedEntity.id).isEqualTo("TX-99999")
        assertThat(savedEntity.userId).isEqualTo("USER-001")
        assertThat(savedEntity.type).isEqualTo(TransactionType.WITHDRAWAL)
        assertThat(savedEntity.amount.compareTo(BigDecimal("4500.00"))).isEqualTo(0)
        assertThat(savedEntity.currencyCode).isEqualTo("KRW")
        assertThat(savedEntity.category).isEqualTo("식비")
        assertThat(savedEntity.memo).isEqualTo("국밥 특")
        assertThat(savedEntity.occurredAt) .isEqualTo(LocalDateTime.of(2026, 9, 15, 12, 30))
    }

    @Test
    @DisplayName("사용자와 조회 기간에 해당하는 거래만 조회한다")
    fun loadTransactionsTest() {
        // given
        adapter.save(
            createTransaction(
                id = "TX-001",
                userId = "USER-001",
                occurredAt = LocalDateTime.of(2026, 9, 1, 0, 0)
            )
        )

        adapter.save(
            createTransaction(
                id = "TX-002",
                userId = "USER-001",
                occurredAt = LocalDateTime.of(2026, 9, 15, 12, 30)
            )
        )

        adapter.save(
            createTransaction(
                id = "TX-003",
                userId = "USER-001",
                occurredAt = LocalDateTime.of(2026, 9, 30, 23, 59, 59)
            )
        )

        // 조회 기간 종료일의 다음 날 00:00
        // 즉, 10월 1일 00:00:00은 조회 대상에서 제외되어야 한다.
        adapter.save(
            createTransaction(
                id = "TX-004",
                userId = "USER-001",
                occurredAt = LocalDateTime.of(2026, 10, 1, 0, 0)
            )
        )

        // 다른 사용자의 거래도 제외되어야 한다.
        adapter.save(
            createTransaction(
                id = "TX-005",
                userId = "USER-002",
                occurredAt = LocalDateTime.of(2026, 9, 15, 12, 30)
            )
        )

        val query = LoadTransactionsQuery(
            userId = "USER-001",
            startDate = LocalDate.of(2026, 9, 1),
            endDate = LocalDate.of(2026, 9, 30)
        )

        // when
        val result = adapter.load(query)

        // then
        assertThat(result)
            .extracting<String> { it.id }
            .containsExactlyInAnyOrder(
                "TX-001",
                "TX-002",
                "TX-003"
            )
    }

    @Test
    @DisplayName("조회 종료일의 다음 날 00시 거래는 조회 대상에서 제외한다")
    fun loadTransactions_excludesEndBoundary() {
        // given
        adapter.save(
            createTransaction(
                id = "TX-INCLUDED",
                userId = "USER-001",
                occurredAt = LocalDateTime.of(2026, 9, 30, 23, 59, 59)
            )
        )

        adapter.save(
            createTransaction(
                id = "TX-EXCLUDED",
                userId = "USER-001",
                occurredAt = LocalDateTime.of(2026, 10, 1, 0, 0)
            )
        )

        val query = LoadTransactionsQuery(
            userId = "USER-001",
            startDate = LocalDate.of(2026, 9, 1),
            endDate = LocalDate.of(2026, 9, 30)
        )

        // when
        val result = adapter.load(query)

        // then
        assertThat(result)
            .extracting<String> { it.id }
            .containsExactly("TX-INCLUDED")
    }

    @Test
    @DisplayName("DB Entity를 Domain 객체로 복원하면서 Money와 거래 발생 시각을 정상적으로 매핑한다")
    fun loadTransactions_mapsEntityToDomain() {
        // given
        adapter.save(
            createTransaction(
                id = "TX-001",
                userId = "USER-001",
                occurredAt = LocalDateTime.of(2026, 9, 15, 12, 30)
            )
        )

        val query = LoadTransactionsQuery(
            userId = "USER-001",
            startDate = LocalDate.of(2026, 9, 15),
            endDate = LocalDate.of(2026, 9, 15)
        )

        // when
        val result = adapter.load(query)

        // then
        assertThat(result).hasSize(1)

        val transaction = result.first()

        assertThat(transaction.id).isEqualTo("TX-001")
        assertThat(transaction.userId).isEqualTo("USER-001")
        assertThat(transaction.type).isEqualTo(TransactionType.WITHDRAWAL)

        assertThat(transaction.amount.amount)
            .isEqualByComparingTo("4500")

        assertThat(transaction.amount.currency.currencyCode)
            .isEqualTo("KRW")

        assertThat(transaction.category).isEqualTo("식비")
        assertThat(transaction.memo).isEqualTo("국밥 특")
        assertThat(transaction.occurredAt)
            .isEqualTo(LocalDateTime.of(2026, 9, 15, 12, 30))
    }

    private fun createTransaction(
        id: String,
        userId: String,
        occurredAt: LocalDateTime
    ): Transaction {
        return Transaction(
            id = id,
            userId = userId,
            type = TransactionType.WITHDRAWAL,
            amount = Money.wons(4500L),
            category = "식비",
            memo = "국밥 특",
            occurredAt = occurredAt
        )
    }
}