package com.concatstudio.onegym.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database as ExposedDatabase

object Database {
    val connection: ExposedDatabase by lazy {
        val databaseConfig = DatabaseConfig.load()
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = databaseConfig.url
            username = databaseConfig.user
            password = databaseConfig.password
            maximumPoolSize = databaseConfig.poolSize
            minimumIdle = 2
            connectionTimeout = 10_000
            poolName = "onegym-postgres-pool"
        }
        ExposedDatabase.connect(HikariDataSource(hikariConfig))
    }
}
