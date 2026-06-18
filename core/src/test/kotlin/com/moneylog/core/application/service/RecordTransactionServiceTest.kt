package com.moneylog.core.application.service

import com.moneylog.core.application.port.inbound.RecordTransactionCommand
import com.moneylog.core.application.port.outbound.SaveTransactionPort
import com.moneylog.core.domain.Transaction
import com.moneylog.core.domain.TransactionType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class RecordTransactionServiceTest {

    // 1. MockK를 이용해 가짜 뒷문(Outbound Port)을 만든다. (DB 연결 안 함!)
    private val saveTransactionPort: SaveTransactionPort = mockk()

    // 2. 테스트 대상(System Under Test) 생성 및 가짜 뒷문 주입
    private val sut: RecordTransactionService = RecordTransactionService(saveTransactionPort)

    @Test
    fun `정상적인 요청이 들어오면 가계부 내역이 올바르게 조립되어 저장된다`() {
        // [Given] 테스트 환경 세팅
        val command = RecordTransactionCommand(
            type = TransactionType.WITHDRAWAL,
            amount = 4500.0,
            currencyCode = "KRW",
            category = "식비",
            memo = "스타벅스 아메리카노",
            timestamp = LocalDateTime.now(),
            userId = "USR-123"
        )

        // 가짜 뒷문이 호출되었을 때의 행동을 미리 정의 (Stubbing)
        // "네가 받은 첫 번째 인자(Transaction)를 그대로 다시 반환해라"
        every { saveTransactionPort.save(any()) } answers { firstArg() }

        // [When] 실제 서비스의 record 메서드 실행
        val result: Transaction = sut.record(command)

        // [Then] 결과 검증 (AssertJ 활용)
        // 1. 날것의 Double 데이터(4500.0)가 Money 객체로 잘 표준화되었는가?
        // Money 클래스의 내부 필드명이 amount인 경우
        // (만약 Money 클래스의 필드명이 value 라면 result.amount.value 로 쓰면 된다)
        assertThat(result.amount.amount).isEqualByComparingTo(4500.0.toBigDecimal())
        assertThat(result.amount.currency.currencyCode).isEqualTo("KRW")

        // 2. 식별자(ID)가 도메인 규칙에 따라 TX- 로 시작하는 UUID로 잘 생성되었는가?
        assertThat(result.id).startsWith("TX-")

        // 3. 나머지 데이터들이 유실 없이 엔티티에 잘 매핑되었는가?
        assertThat(result.type).isEqualTo(TransactionType.WITHDRAWAL)
        assertThat(result.memo).isEqualTo("스타벅스 아메리카노")

        // 4. [행위 검증] 실제로 뒷문의 save() 메서드가 딱 1번(exactly = 1) 호출되었는가?
        verify(exactly = 1) { saveTransactionPort.save(any()) }
    }

    @Test
    fun `결제 금액이 0원 이하이면 서비스 로직을 타기도 전에 입구에서 차단된다`() {
        // [Given & When & Then]
        // 0원의 쓰레기 데이터로 Command를 생성하려고 시도하면,
        assertThatThrownBy {
            RecordTransactionCommand(
                type = TransactionType.WITHDRAWAL,
                amount = 0.0, // ❌ 쓰레기 데이터 투입
                currencyCode = "KRW",
                category = "식비",
                memo = "공짜 커피",
                timestamp = LocalDateTime.now(),
                userId = "USR-123"
            )
        }
            // IllegalArgumentException이 발생해야 하며, 에러 메시지를 포함해야 한다.
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("결제 금액은 0보다 커야 한다")
    }
}