package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.CoachType

interface CoachTypeRepository {
    fun getCoachTypes(): List<CoachType>
    fun getCoachTypeById(id: Long): CoachType?
    fun createCoachType(coachType: CoachType)
    fun updateCoachType(id: Long, coachType: CoachType)
    fun deleteCoachTypeById(id: Long)
}
