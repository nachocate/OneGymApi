package com.concatstudio.onegym.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

object PasswordService {
    private val encoder = BCryptPasswordEncoder(12)

    fun encode(rawPassword: String): String =
        encoder.encode(rawPassword) ?: error("Password encoder returned null")

    fun matches(rawPassword: String, encodedPassword: String): Boolean =
        encoder.matches(rawPassword, encodedPassword)
}
