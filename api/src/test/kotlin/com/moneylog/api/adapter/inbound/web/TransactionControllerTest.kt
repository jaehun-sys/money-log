package com.moneylog.api.adapter.inbound.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.moneylog.core.application.port.inbound.RecordTransactionUseCase
import com.moneylog.core.domain.Transaction
import com.moneylog.core.domain.TransactionType
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.LocalDateTime

@WebMvcTest(TransactionController::class)
class RecordTransactionControllerTest {
    //스프링 프레임워크 환경에서는 객체를 new로 직접 만들지 않고, 스프링이 나중에 런타임에 꽂아주는(Injection) 경우가 많다. 이때 사용하는 것이 lateinit var다.
    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper
    @MockkBean private lateinit var recordTransactionUseCase: RecordTransactionUseCase

    @Test
    fun `정상적인 거래 등록 요청은 200을 반환하고 timestamp를 서버 시간으로 주입한다`() {
        // given
        val beforeRequest = LocalDateTime.now()

        val transaction = io.mockk.mockk<Transaction> {
            every { id } returns "TX-TEST-001"
        }

        every {
            recordTransactionUseCase.record(any())
        } returns transaction

        val request = validRequest().apply {
            remove("timestamp")
        }

        // when
        val result = mockMvc.post("/api/v1/transactions") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }

        val afterRequest = LocalDateTime.now()

        // then
        result.andExpect {
            status { isOk() }
            jsonPath("$.transactionId") {
                value("TX-TEST-001")
            }
        }

        verify(exactly = 1) {
            recordTransactionUseCase.record(
                match {
                    it.type == TransactionType.WITHDRAWAL &&
                            it.amount.toPlainString() == "4500" &&
                            it.currencyCode == "KRW" &&
                            it.category == "식비" &&
                            it.memo == "테스트" &&
                            it.userId == "USR-123" &&
                            it.timestamp >= beforeRequest &&
                            it.timestamp <= afterRequest
                }
            )
        }
    }

    @Test
    fun `timestamp가 명시되면 해당 값을 그대로 Command에 전달한다`() {
        // given
        val timestamp = LocalDateTime.of(
            2026,
            9,
            20,
            18,
            30,
            15
        )

        val transaction = io.mockk.mockk<Transaction> {
            every { id } returns "TX-TEST-002"
        }

        every {
            recordTransactionUseCase.record(any())
        } returns transaction

        val request = validRequest().apply {
            this["timestamp"] = timestamp.toString()
        }

        // when
        val result = mockMvc.post("/api/v1/transactions") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }

        // then
        result.andExpect {
            status { isOk() }
            jsonPath("$.transactionId") {
                value("TX-TEST-002")
            }
        }

        verify(exactly = 1) {
            recordTransactionUseCase.record(
                match {
                    it.timestamp == timestamp
                }
            )
        }
    }

    @Test
    fun `amount가 null이면 400을 반환하고 UseCase를 호출하지 않는다`() {
        val request = validRequest().apply {
            this["amount"] = null
        }

        mockMvc.post("/api/v1/transactions") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) {
            recordTransactionUseCase.record(any())
        }
    }

    @Test
    fun `amount가 0이면 400을 반환하고 UseCase를 호출하지 않는다`() {
        val request = validRequest().apply {
            this["amount"] = 0
        }

        mockMvc.post("/api/v1/transactions") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) {
            recordTransactionUseCase.record(any())
        }
    }

    @Test
    fun `amount가 음수이면 400을 반환하고 UseCase를 호출하지 않는다`() {
        val request = validRequest().apply {
            this["amount"] = -100
        }

        mockMvc.post("/api/v1/transactions") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) {
            recordTransactionUseCase.record(any())
        }
    }

    @Test
    fun `type이 null이면 400을 반환한다`() {
        val request = validRequest().apply {
            this["type"] = null
        }

        post(request).andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) {
            recordTransactionUseCase.record(any())
        }
    }

    @Test
    fun `currency가 null이면 400을 반환한다`() {
        val request = validRequest().apply {
            this["currency"] = null
        }

        post(request).andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) {
            recordTransactionUseCase.record(any())
        }
    }

    @Test
    fun `currency가 빈 문자열이면 400을 반환한다`() {
        val request = validRequest().apply {
            this["currency"] = ""
        }

        post(request).andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) {
            recordTransactionUseCase.record(any())
        }
    }

    @Test
    fun `category가 null이면 400을 반환한다`() {
        val request = validRequest().apply {
            this["category"] = null
        }

        post(request).andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) {
            recordTransactionUseCase.record(any())
        }
    }

    @Test
    fun `category가 빈 문자열이면 400을 반환한다`() {
        val request = validRequest().apply {
            this["category"] = ""
        }

        post(request).andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) {
            recordTransactionUseCase.record(any())
        }
    }

    @Test
    fun `userId가 null이면 400을 반환한다`() {
        val request = validRequest().apply {
            this["userId"] = null
        }

        post(request).andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) {
            recordTransactionUseCase.record(any())
        }
    }

    @Test
    fun `userId가 빈 문자열이면 400을 반환한다`() {
        val request = validRequest().apply {
            this["userId"] = ""
        }

        post(request).andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) {
            recordTransactionUseCase.record(any())
        }
    }

    @Test
    fun `잘못된 통화 코드가 전달되면 GlobalExceptionHandler가 400으로 변환한다`() {
        // given
        val transaction = io.mockk.mockk<Transaction> {
            every { id } returns "TX-TEST-003"
        }

        every {
            recordTransactionUseCase.record(any())
        } throws IllegalArgumentException("Unknown currency")

        val request = validRequest().apply {
            this["currency"] = "INVALID"
        }

        // when
        val result = post(request)

        // then
        result.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 1) {
            recordTransactionUseCase.record(
                match {
                    it.currencyCode == "INVALID"
                }
            )
        }
    }

    private fun validRequest(): MutableMap<String, Any?> =
        mutableMapOf(
            "type" to "WITHDRAWAL",
            "amount" to 4500,
            "currency" to "KRW",
            "category" to "식비",
            "memo" to "테스트",
            "userId" to "USR-123"
        )

    private fun post(
        request: Map<String, Any?>
    ) = mockMvc.post("/api/v1/transactions") {
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
    }
}