package com.moneylog.api.adapter.inbound.web

import com.moneylog.api.adapter.inbound.web.dto.RecordTransactionRequest
import com.moneylog.api.adapter.inbound.web.dto.TransactionResponse
import com.moneylog.core.application.port.inbound.LoadTransactionsQuery
import com.moneylog.core.application.port.inbound.LoadTransactionsUseCase
import com.moneylog.core.application.port.inbound.RecordTransactionCommand
import com.moneylog.core.application.port.inbound.RecordTransactionUseCase
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

/**
 * [Inbound Web Adapter] 거래 등록 REST Controller(거래 REST API)
 *
 * - 책임: HTTP 프로토콜 해석, 입력값 검증 트리거(@Valid), UseCase 호출, HTTP 응답 매핑.
 * - 어댑터는 도메인 객체(Money, Transaction)를 직접 생성하거나 조립하지 않으며,
 *   유스케이스가 요구하는 Command 규격으로 단순 번역하여 포트로 전달한다.
 *
 * HTTP 프로토콜을 해석하고 Core UseCase를 호출한다.
 *
 * Controller는 도메인 객체를 직접 조립하지 않는다.
 */
@RestController
@RequestMapping("/api/v1/transactions")
class TransactionController(
    private val recordTransactionUseCase: RecordTransactionUseCase,
    private val loadTransactionsUseCase: LoadTransactionsUseCase
) {

    @PostMapping
    fun recordTransaction(
        @Valid @RequestBody request: RecordTransactionRequest
    ): ResponseEntity<Map<String, String>> {

        val command = RecordTransactionCommand(
            type = request.type!!,
            amount = request.amount!!,
            currencyCode = request.currency!!.uppercase(Locale.ROOT),
            category = request.category!!,
            memo = request.memo,
            occurredAt = request.occurredAt!!,
            userId = request.userId!!
        )

        val transaction = recordTransactionUseCase.record(command)

        return ResponseEntity.ok(
            mapOf("transactionId" to transaction.id)
        )
    }

    @GetMapping
    fun loadTransactions(
        @RequestParam userId: String,
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate,
    ): ResponseEntity<List<TransactionResponse>> {

        val query = LoadTransactionsQuery(
            userId = userId,
            startDate = startDate,
            endDate = endDate
        )

        val transactions = loadTransactionsUseCase.load(query)

        return ResponseEntity.ok(
            transactions.map(TransactionResponse::from)
        )
    }
}