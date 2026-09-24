package com.moneylog.api.config

import com.moneylog.core.application.port.inbound.LoadTransactionsUseCase
import com.moneylog.core.application.port.inbound.RecordTransactionUseCase
import com.moneylog.core.application.port.outbound.LoadTransactionsPort
import com.moneylog.core.application.port.outbound.SaveTransactionPort
import com.moneylog.core.application.service.LoadTransactionsService
import com.moneylog.core.application.service.RecordTransactionService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Core Application Service를 Spring Bean으로 조립하는 설정.
 *
 * Core는 Spring Framework에 의존하지 않으므로,
 * API 모듈의 Composition Root에서 Application Service를 Bean으로 등록한다.
 */
@Configuration
class ApiUseCaseConfig {

    @Bean
    fun recordTransactionUseCase(
        saveTransactionPort: SaveTransactionPort
    ): RecordTransactionUseCase {
        // Storage 모듈에 있는 Adapter(@Component)가 saveTransactionPort로 주입된다.
        return RecordTransactionService(saveTransactionPort)
    }

    @Bean
    fun loadTransactionsUseCase(
        loadTransactionsPort: LoadTransactionsPort
    ): LoadTransactionsUseCase {
        // Storage 모듈에 있는 Adapter(@Component)가 loadTransactionsPort로 주입된다.
        return LoadTransactionsService(loadTransactionsPort)
    }
}
