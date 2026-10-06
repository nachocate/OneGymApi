package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.Day

interface DayRepository {
    fun getDays(): List<Day>
    fun getDayById(id: Long): Day?
    fun updateDay(dayId: Long, day: Day)
    fun createDay(day: Day)
    fun deleteDayById(id: Long)
}
