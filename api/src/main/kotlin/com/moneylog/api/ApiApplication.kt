package com.moneylog.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * [Web Adapter 모듈 진입점]
 * Spring Boot 애플리케이션의 심장이자 컴포넌트 스캔의 시작점.
 */
@SpringBootApplication
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}