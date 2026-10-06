package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.*

internal fun userRef(id: Long) = UserDao.findById(id)!!.id
internal fun gymRef(id: Long) = GymDao.findById(id)!!.id
internal fun roleRef(id: Long) = UserRoleDao.findById(id)!!.id
internal fun coachTypeRef(id: Long) = CoachTypeDao.findById(id)!!.id
internal fun planTypeRef(id: Long) = PlanTypeDao.findById(id)!!.id
internal fun planRef(id: Long) = PlanDao.findById(id)!!.id
internal fun weekRef(id: Long) = WeekDao.findById(id)!!.id
internal fun dayRef(id: Long) = DayDao.findById(id)!!.id
internal fun exerciseRef(id: Long) = ExerciseDao.findById(id)!!.id
internal fun quantityTypeRef(id: Long) = QuantityTypeDao.findById(id)!!.id
internal fun blockRef(id: Long) = BlockDao.findById(id)!!.id
internal fun userPlanRef(id: Long) = UserPlanDao.findById(id)!!.id
