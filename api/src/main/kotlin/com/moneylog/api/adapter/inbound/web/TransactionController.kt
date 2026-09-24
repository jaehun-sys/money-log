package com.moneylog.api.adapter.inbound.web

import com.moneylog.api.adapter.inbound.web.dto.RecordTransactionRequest
import com.moneylog.core.application.port.inbound.RecordTransactionCommand
import com.moneylog.core.application.port.inbound.RecordTransactionUseCase
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.Locale

/**
 * [Inbound Web Adapter] 거래 등록 REST Controller
 *
 * - 책임: HTTP 프로토콜 해석, 입력값 검증 트리거(@Valid), UseCase 호출, HTTP 응답 매핑.
 * - 어댑터는 도메인 객체(Money, Transaction)를 직접 생성하거나 조립하지 않으며,
 *   유스케이스가 요구하는 Command 규격으로 단순 번역하여 포트로 전달한다.
 */
@RestController
@RequestMapping("/api/v1/transactions")
class RecordTransactionController(
    private val recordTransactionUseCase: RecordTransactionUseCase
) {

    @PostMapping
    fun recordTransaction(
        @Valid @RequestBody request: RecordTransactionRequest
    ): ResponseEntity<Map<String, String>> {

        // HTTP 요청 시각이 누락된 경우, 요청이 웹 어댑터 경계에 도달한 시점을 확정하여 Command에 주입한다.
        val command = RecordTransactionCommand(
            type = request.type!!,
            amount = request.amount!!,
            currencyCode = request.currency!!.uppercase(Locale.ROOT),
            category = request.category!!,
            memo = request.memo,
            timestamp = request.timestamp ?: LocalDateTime.now(),
            userId = request.userId!!
        )

        val transaction = recordTransactionUseCase.record(command)

        return ResponseEntity.ok(
            mapOf("transactionId" to transaction.id)
        )
    }
}