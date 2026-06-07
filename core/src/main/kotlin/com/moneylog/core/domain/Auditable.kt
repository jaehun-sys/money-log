package com.moneylog.core.domain

import java.time.LocalDateTime

interface Auditable {
    val createdAt: LocalDateTime // 생성 일시
    val createdBy: String // 생성자 (사용자ID 또는 시스템)
    val createdPgmId: String // 생성 프로그램 (예:"APP_IOS", "WEB_BACK")

    val updatedAt: LocalDateTime? // 처음 생성될 땐 수정 기록이 없으니 Nullable(?)
    val updatedBy: String?
    val updatedPgmId: String?
}