package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.CoachResponse

interface GymHomeRepository {
    fun getActiveCoaches(gymId: Long): List<CoachResponse>
}
