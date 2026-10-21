package com.concatstudio.onegym

import com.concatstudio.onegym.model.SaveCurrentPlanActivityRequest
import com.concatstudio.onegym.model.SaveRegisterRequest
import com.concatstudio.onegym.model.SaveUserPlanRequest
import com.concatstudio.onegym.model.toModel
import kotlin.test.Test
import kotlin.test.assertEquals

class TrainingRequestsTest {
    @Test
    fun `write requests map to server controlled models with generated id`() {
        assertEquals(0, SaveUserPlanRequest(userId = 2, planId = 3).toModel().id)
        assertEquals(0, SaveCurrentPlanActivityRequest("2026-10-12T12:00:00Z", 4, null, null).toModel().id)
        assertEquals(0, SaveRegisterRequest(4, 5, 6, 42.5).toModel().id)
    }
}
