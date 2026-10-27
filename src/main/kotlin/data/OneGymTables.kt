package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.Users
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.time
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone

object Countries : LongIdTable("countries") {
    val name = varchar("name", 100)
    val isoCode = char("iso_code", 2)
}

object Provinces : LongIdTable("provinces") {
    val country = reference("country_id", Countries)
    val name = varchar("name", 100)
}

object Localities : LongIdTable("localities") {
    val province = reference("province_id", Provinces)
    val name = varchar("name", 150)
}

object Gyms : LongIdTable("gyms") {
    val name = varchar("name", 150)
    val description = text("description").nullable()
    val locality = reference("locality_id", Localities)
    val address = varchar("address", 255).nullable()
    val phone = varchar("phone", 50).nullable()
    val logoUrl = text("logo_url").nullable()
    val bannerUrl = text("banner_url").nullable()
}

object GymBusinessHours : LongIdTable("gym_business_hours") {
    val gym = reference("gym_id", Gyms)
    val dayOfWeek = short("day_of_week")
    val opensAt = time("opens_at")
    val closesAt = time("closes_at")
}

object UserRoles : LongIdTable("user_roles") {
    val name = varchar("name", 100).uniqueIndex()
}

object SystemRoles : LongIdTable("system_roles") {
    val name = varchar("name", 100).uniqueIndex()
}

object UserSystemRoles : LongIdTable("user_system_roles") {
    val user = reference("user_id", Users)
    val systemRole = reference("system_role_id", SystemRoles)
}

object GymSubscriptions : LongIdTable("gym_subscriptions") {
    val gym = reference("gym_id", Gyms)
    val startDate = date("start_date")
    val expiresAt = date("expires_at").nullable()
    val isActive = bool("is_active").default(true)
}

object CoachTypes : LongIdTable("coach_types") {
    val name = varchar("name", 100).uniqueIndex()
}

object UserGyms : LongIdTable("user_gyms") {
    val user = reference("user_id", Users)
    val gym = reference("gym_id", Gyms)
    val role = reference("role_id", UserRoles)
    val startDate = date("start_date")
    val endDate = date("end_date").nullable()
}

object GymCoaches : LongIdTable("gym_coaches") {
    val user = reference("user_id", Users)
    val gym = reference("gym_id", Gyms)
    val coachType = reference("coach_type_id", CoachTypes)
    val startDate = date("start_date")
    val endDate = date("end_date").nullable()
}

object UserRefreshTokens : LongIdTable("user_refresh_tokens") {
    val user = reference("user_id", Users)
    val tokenHash = varchar("token_hash", 64).uniqueIndex()
    val deviceInfo = varchar("device_info", 100).nullable()
    val expiresAt = timestampWithTimeZone("expires_at")
    val isRevoked = bool("is_revoked").default(false)
    val createdAt = timestampWithTimeZone("created_at")
}

object News : LongIdTable("news") {
    val gym = reference("gym_id", Gyms)
    val title = varchar("title", 255)
    val description = text("description")
    val imageUrl = text("image_url").nullable()
    val date = timestampWithTimeZone("date")
}

object PlanTypes : LongIdTable("plan_types") {
    val name = varchar("name", 100).uniqueIndex()
}

object Plans : LongIdTable("plans") {
    val name = varchar("name", 150)
    val gym = reference("gym_id", Gyms)
    val planType = reference("plan_type_id", PlanTypes)
    val planRoot = reference("plan_root_id", Plans).nullable()
    val revision = long("revision").default(1)
}

object Weeks : LongIdTable("weeks") {
    val number = integer("number")
    val plan = reference("plan_id", Plans)
}

object Days : LongIdTable("days") {
    val number = integer("number")
    val week = reference("week_id", Weeks)
}

object Exercises : LongIdTable("exercises") {
    val name = varchar("name", 150)
    val description = text("description").nullable()
    val videoUrl = text("video_url").nullable()
    val imageUrl = text("image_url").nullable()
    val gym = reference("gym_id", Gyms).nullable()
    val isVisibleGlobal = bool("is_visible_global").default(true)
}

object QuantityTypes : LongIdTable("quantity_types") {
    val name = varchar("name", 100).uniqueIndex()
}

object Blocks : LongIdTable("blocks") {
    val position = integer("position")
    val name = varchar("name", 150).nullable()
    val day = reference("day_id", Days)
    val laps = integer("laps")
}

object BlockExercises : LongIdTable("block_exercises") {
    val block = reference("block_id", Blocks)
    val exercise = reference("exercise_id", Exercises)
    val quantityType = reference("quantity_type_id", QuantityTypes).nullable()
    val position = integer("position")
    val repetitions = integer("repetitions").nullable()
    val quantity = decimal("quantity", 10, 2).nullable()
}

object UserPlans : LongIdTable("user_plans") {
    val user = reference("user_id", Users)
    val plan = reference("plan_id", Plans)
}

object CurrentPlanActivities : LongIdTable("current_plan_activities") {
    val date = timestampWithTimeZone("date")
    val userPlan = reference("user_plan_id", UserPlans)
    val activeWeek = reference("active_week_id", Weeks).nullable()
    val activeDay = reference("active_day_id", Days).nullable()
    val isActive = bool("is_active").default(false)
}

object Registers : LongIdTable("registers") {
    val userPlan = reference("user_plan_id", UserPlans)
    val exercise = reference("exercise_id", Exercises)
    val block = reference("block_id", Blocks)
    val weight = decimal("weight", 10, 2)
}

object Evaluations : LongIdTable("evaluations") {
    val user = reference("user_id", Users)
    val name = varchar("name", 150)
    val description = text("description").nullable()
    val creationDate = timestampWithTimeZone("creation_date")
}

object UserTests : LongIdTable("user_tests") {
    val exercise = reference("exercise_id", Exercises)
    val user = reference("user_id", Users)
    val evaluation = reference("evaluation_id", Evaluations)
    val quantityType = reference("quantity_type_id", QuantityTypes).nullable()
    val repetitions = integer("repetitions").nullable()
    val quantity = decimal("quantity", 10, 2).nullable()
    val date = timestampWithTimeZone("date")
    val description = text("description")
}
