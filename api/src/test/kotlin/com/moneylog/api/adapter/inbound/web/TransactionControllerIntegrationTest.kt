package com.moneylog.api.adapter.inbound.web

import com.moneylog.core.application.port.inbound.RecordTransactionCommand
import com.moneylog.core.application.port.inbound.RecordTransactionUseCase
import com.moneylog.core.domain.TransactionType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@Transactional  // 테스트 종료 후 트랜잭션을 롤백하여 테스트 데이터를 격리한다.
class TransactionControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var recordTransactionUseCase: RecordTransactionUseCase

    @BeforeEach
    fun setUp() {
        recordTransactionUseCase.record(
            RecordTransactionCommand(
                type = TransactionType.WITHDRAWAL,
                amount = BigDecimal("4500"),
                currencyCode = "KRW",
                category = "식비",
                memo = "점심",
                occurredAt = LocalDateTime.of(2026, 9, 20, 12, 30),
                userId = "USR-123"
            )
        )

        recordTransactionUseCase.record(
            RecordTransactionCommand(
                type = TransactionType.DEPOSIT,
                amount = BigDecimal("100000"),
                currencyCode = "KRW",
                category = "급여",
                memo = "9월 급여",
                occurredAt = LocalDateTime.of(2026, 9, 25, 9, 0),
                userId = "USR-123"
            )
        )

        recordTransactionUseCase.record(
            RecordTransactionCommand(
                type = TransactionType.WITHDRAWAL,
                amount = BigDecimal("12000"),
                currencyCode = "KRW",
                category = "식비",
                memo = "저녁",
                occurredAt = LocalDateTime.of(2026, 9, 20, 18, 0),
                userId = "USR-999"
            )
        )
    }

    @Test
    fun `GET 거래 내역 조회 - 실제 계층을 거쳐 거래 내역을 반환한다`() {
        mockMvc.get("/api/v1/transactions") {
            param("userId", "USR-123")
            param("startDate", "2026-09-20")
            param("endDate", "2026-09-20")
        }
            .andExpect {
                status().isOk

                jsonPath("$.length()").value(1)
                jsonPath("$[0].userId").value("USR-123")
                jsonPath("$[0].type").value("WITHDRAWAL")
                jsonPath("$[0].amount").value(4500)
                jsonPath("$[0].currency").value("KRW")
                jsonPath("$[0].category").value("식비")
                jsonPath("$[0].memo").value("점심")
                jsonPath("$[0].occurredAt").value("2026-09-20T12:30:00")
            }
    }

    @Test
    fun `GET 거래 내역 조회 - 사용자와 날짜 조건으로 거래 내역을 필터링한다`() {
        mockMvc.get("/api/v1/transactions") {
            param("userId", "USR-123")
            param("startDate", "2026-09-21")
            param("endDate", "2026-09-30")
        }
            .andExpect {
                status().isOk

                jsonPath("$.length()").value(1)
                jsonPath("$[0].userId").value("USR-123")
                jsonPath("$[0].type").value("DEPOSIT")
                jsonPath("$[0].amount").value(100000)
                jsonPath("$[0].category").value("급여")
                jsonPath("$[0].occurredAt").value("2026-09-25T09:00:00")
            }
    }

    @Test
    fun `GET 거래 내역 조회 - 잘못된 날짜 형식은 400을 반환한다`() {
        mockMvc.get("/api/v1/transactions") {
            param("userId", "USR-123")
            param("startDate", "2026/09/20")
            param("endDate", "2026-09-20")
        }
            .andExpect {
                status().isBadRequest
            }
    }
}
