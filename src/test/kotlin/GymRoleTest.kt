package com.concatstudio.onegym

import com.concatstudio.onegym.security.GymRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GymRoleTest {
    @Test
    fun `parses supported gym roles without case sensitivity`() {
        assertEquals(GymRole.OWNER, GymRole.fromName("owner"))
        assertEquals(GymRole.COACH, GymRole.fromName(" COACH "))
        assertNull(GymRole.fromName("super_admin"))
    }
}
