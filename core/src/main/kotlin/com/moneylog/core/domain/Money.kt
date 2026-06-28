package com.moneylog.core.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

/**
 * Money-Log 시스템 내에서 '화폐의 가치' 그 자체를 대변하는 가장 작은 원자(Atomic) 단위.
 * * [비즈니스 규칙 및 책임]
 * 1. 가치 보존의 원칙: 한 번 생성된 금액은 위변조가 불가능하다. 금액이 변경되어야 한다면 기존 가치를 수정하는 것이 아니라 새로운 가치로 파생되어야 한다.
 * 2. 교환 불가 원칙: 서로 다른 통화(예: KRW와 USD) 간의 무분별한 단순 사칙연산을 엄격히 금지하여, 환율이 적용되지 않은 금융 사고를 원천 차단한다.
 * 3. 무손실 계산: 컴퓨터의 소수점 연산 한계로 인한 금액 오차를 방지하여, 단 1원의 누수도 없는 정확한 정산을 보장한다.
 * * 이 객체는 단순한 숫자의 모음이 아니라, 시스템 내에서 신뢰하고 주고받을 수 있는 '실제 현금'으로 취급된다.
 */
data class Money private constructor(
    val amount: BigDecimal,
    val currency: Currency
) {
    // 1. 더하기 연산 (+)
    operator fun plus(other: Money): Money {
        require(this.currency == other.currency) { "통화 단위가 다릅니다: ${this.currency} != ${other.currency}" }
        return Money(this.amount.add(other.amount), this.currency)
    }

    // 2. 빼기 연산 (-)
    operator fun minus(other: Money): Money {
        require(this.currency == other.currency) { "통화 단위가 다릅니다: ${this.currency} != ${other.currency}" }
        return Money(this.amount.subtract(other.amount), this.currency)
    }

    // 3. 곱하기 연산 (*)
    operator fun times(multiplier: Int): Money {
        return Money(this.amount.multiply(BigDecimal(multiplier)), this.currency)
    }

    // 4. 나누기 연산 (/ 및 반올림 처리)
    fun divide(divisor: Int, roundingMode: RoundingMode = RoundingMode.HALF_UP): Money {
        return Money(this.amount.divide(BigDecimal(divisor), roundingMode), this.currency)
    }

    // 5. 안전한 생성을 위한 팩토리 메서드 (안전장치)
    companion object {
        private val KRW: Currency = Currency.getInstance("KRW")

        // 0원 초기화 (기본값 원화)
        fun zero(currency: Currency = KRW): Money {
            return Money(BigDecimal.ZERO, currency)
        }

        // Long 타입의 안전한 원화 생성
        fun wons(amount: Long): Money {
            return Money(BigDecimal.valueOf(amount), KRW)
        }

        // 일반적인 생성 (Long)
        fun of(amount: Long, currency: Currency): Money {
            return Money(BigDecimal.valueOf(amount), currency)
        }

        // 위험한 Double 타입의 안전한 생성 (소수점 오차 방어)
        fun of(amount: Double, currency: Currency): Money {
            // Double을 String으로 변환 후 BigDecimal로 만들어 소수점 깨짐을 원천 차단
            return Money(BigDecimal(amount.toString()), currency)
        }

        // 💥 [추가] 금융 연산의 근본이자 DB에서 올라온 데이터를 받아줄 팩토리!
        fun of(amount: BigDecimal, currency: Currency): Money {
            return Money(amount, currency)
        }
    }
}