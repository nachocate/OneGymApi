package com.concatstudio.onegym

import com.concatstudio.onegym.security.PasswordService
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PasswordServiceTest {
    @Test
    fun `encodes and verifies passwords`() {
        val hash = PasswordService.encode("correct horse battery staple")

        assertTrue(PasswordService.matches("correct horse battery staple", hash))
        assertFalse(PasswordService.matches("wrong password", hash))
    }

    @Test
    fun `validates the password used by the seeded users`() {
        val seededHash = "\$2a\$12\$lRjI71wrqakSjaVFLQZSZ.xmq2.wn6LbBGQgJWSGndpobIG0ygwl6"

        assertTrue(PasswordService.matches("password", seededHash), seededHash)
    }
}
