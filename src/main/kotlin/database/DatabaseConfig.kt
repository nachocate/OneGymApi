package com.concatstudio.onegym.database

import com.typesafe.config.ConfigFactory

data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
    val poolSize: Int
) {
    companion object {
        fun load(): DatabaseConfig {
            val config = ConfigFactory.load().getConfig("database")

            return DatabaseConfig(
                url = System.getenv("DB_URL") ?: config.getString("url"),
                user = System.getenv("DB_USER") ?: config.getString("user"),
                password = System.getenv("DB_PASSWORD") ?: config.getString("password"),
                poolSize = (System.getenv("DB_POOL_SIZE")
                    ?: config.getInt("pool-size").toString()).toInt()
            )
        }
    }
}
