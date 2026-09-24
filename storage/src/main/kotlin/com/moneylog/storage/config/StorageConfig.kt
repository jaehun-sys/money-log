package com.moneylog.storage.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EntityScan(basePackages = ["com.moneylog.storage.persistence.entity"])
@EnableJpaRepositories(basePackages = ["com.moneylog.storage.persistence.repository"])
class StorageConfig
