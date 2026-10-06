package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.Users
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone

object Gyms : LongIdTable("gyms") {
    val name = varchar("name", 150)
    val description = text("description").nullable()
    val schedule = text("schedule").nullable()
    val address = varchar("address", 255).nullable()
    val phone = varchar("phone", 50).nullable()
    val logoUrl = text("logo_url").nullable()
    val bannerUrl = text("banner_url").nullable()
}

object UserRoles : LongIdTable("user_roles") {
    val name = varchar("name", 100).uniqueIndex()
}

object UserGyms : LongIdTable("user_gyms") {
    val user = reference("id_user", Users)
    val gym = reference("id_gym", Gyms)
    val role = reference("id_user_rol", UserRoles)
    val startDate = date("start_date")
    val endDate = date("end_date").nullable()
}

object News : LongIdTable("news") {
    val gym = reference("id_gym", Gyms)
    val title = varchar("title", 255)
    val description = text("description")
    val date = timestampWithTimeZone("date")
}

object PlanTypes : LongIdTable("plan_types") {
    val name = varchar("name", 100).uniqueIndex()
}

object Plans : LongIdTable("plans") {
    val name = varchar("name", 150)
    val gym = reference("id_gym", Gyms)
    val planType = reference("id_plan_type", PlanTypes)
    val planRoot = reference("id_plan_root", Plans).nullable()
}

object Weeks : LongIdTable("weeks") {
    val number = integer("number")
    val plan = reference("id_plan", Plans)
}

object Days : LongIdTable("days") {
    val number = integer("number")
    val week = reference("id_week", Weeks)
}

object Exercises : LongIdTable("exercises") {
    val name = varchar("name", 150)
    val description = text("description").nullable()
    val videoUrl = text("video_url").nullable()
    val imageUrl = text("image_url").nullable()
}

object QuantityTypes : LongIdTable("quantity_types") {
    val name = varchar("name", 100).uniqueIndex()
}

object Blocks : LongIdTable("blocks") {
    val position = integer("position")
    val name = varchar("name", 150).nullable()
    val day = reference("id_day", Days)
    val laps = integer("laps")
}

object BlockExercises : LongIdTable("block_exercises") {
    val block = reference("id_block", Blocks)
    val exercise = reference("id_exercise", Exercises)
    val quantityType = reference("id_quantity_type", QuantityTypes).nullable()
    val position = integer("position")
    val repetitions = integer("repetitions").nullable()
    val quantity = decimal("quantity", 10, 2).nullable()
}

object UserPlans : LongIdTable("user_plans") {
    val user = reference("id_user", Users)
    val plan = reference("id_plan", Plans)
}

object CurrentPlanActivities : LongIdTable("current_plan_activities") {
    val date = timestampWithTimeZone("date")
    val userPlan = reference("id_user_plan", UserPlans)
    val activeWeek = reference("id_week_active", Weeks).nullable()
    val activeDay = reference("id_day_active", Days).nullable()
}

object Registers : LongIdTable("registers") {
    val userPlan = reference("id_user_plan", UserPlans)
    val exercise = reference("id_exercise", Exercises)
    val block = reference("id_block", Blocks)
    val weight = decimal("weight", 10, 2)
}

object UserTests : LongIdTable("user_tests") {
    val exercise = reference("id_exercise", Exercises)
    val user = reference("id_user", Users)
    val quantityType = reference("id_quantity_type", QuantityTypes).nullable()
    val repetitions = integer("repetitions").nullable()
    val quantity = decimal("quantity", 10, 2).nullable()
    val date = timestampWithTimeZone("date")
    val description = text("description")
}
