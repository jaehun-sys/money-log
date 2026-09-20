package com.moneylog.api.adapter.inbound.web.dto

import com.moneylog.core.domain.TransactionType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * [Web Request DTO] 거래 등록 HTTP 요청 본문 매핑 객체
 *
 * - 책임: HTTP JSON 역직렬화 및 외부 입력의 구문/형식적 유효성(HTTP/Web Validation) 검증.
 * - 코틀린 Reflection 및 Bean Validation 정상 동작을 위해 필수 필드는 Nullable(`?`)로 선언하고 어노테이션으로 제어한다.
 */
data class RecordTransactionRequest(
    @field:NotNull(message = "거래 유형은 필수입니다.")
    val type: TransactionType?,

    @field:NotNull(message = "금액은 필수입니다.")
    @field:Positive(message = "금액은 0보다 커야 합니다.")
    val amount: BigDecimal?,

    @field:NotBlank(message = "통화 코드는 필수입니다.")
    val currency: String?,

    @field:NotBlank(message = "카테고리는 필수입니다.")
    val category: String?,

    val memo: String = "",

    val timestamp: LocalDateTime? = null,

    @field:NotBlank(message = "사용자 식별자는 필수입니다.")
    val userId: String?
)