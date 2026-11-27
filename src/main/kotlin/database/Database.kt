package com.concatstudio.onegym.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.pool.HikariPool.PoolInitializationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.Database as ExposedDatabase
import java.sql.SQLException

object Database {
    private val dataSource: HikariDataSource by lazy {
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
        HikariDataSource(hikariConfig)
    }

    val connection: ExposedDatabase by lazy { ExposedDatabase.connect(dataSource) }

    suspend fun isReady(dispatcher: CoroutineDispatcher = Dispatchers.IO): Boolean = withContext(dispatcher) {
        try {
            dataSource.connection.use { it.isValid(2) }
        } catch (_: SQLException) {
            false
        } catch (_: PoolInitializationException) {
            false
        }
    }
}
