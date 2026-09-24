package com.moneylog.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * [Web Adapter 모듈 진입점]
 * Spring Boot 애플리케이션의 심장이자 컴포넌트 스캔의 시작점.
 */
// Spring 빈 스캔 범위를 com.moneylog 전체로 확장
@SpringBootApplication(scanBasePackages = ["com.moneylog"])
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}