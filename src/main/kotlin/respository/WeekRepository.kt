package com.concatstudio.onegym.respository

import com.concatstudio.onegym.model.Week

interface WeekRepository {
    fun getWeeks(): List<Week>
    fun getWeekById(id: Long): Week?
    fun updateWeek(weekId: Long, week: Week)
    fun createWeek(week: Week)
    fun deleteWeekById(id: Long)
}
