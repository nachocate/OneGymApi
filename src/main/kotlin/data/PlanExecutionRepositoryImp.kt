package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.Users
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.model.*
import com.concatstudio.onegym.respository.PlanExecutionRepository
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.*
import kotlin.math.roundToInt

class PlanExecutionRepositoryImp : PlanExecutionRepository {
    override fun start(gymId: Long, userId: Long, userPlanId: Long, planDayId: Long) = transaction(Database.connection) {
        val zone = gymZone(gymId)
        val now = ZonedDateTime.now(zone)
        finalizeExpired(gymId, now)
        val today = now.toLocalDate()
        // Reuse the gym-scoped joined row: toResponse needs UserPlans.plan as well as
        // the execution columns. A bare PlanExecutions row caused retries to fail.
        existingExecution(gymId, userPlanId, today)?.let { return@transaction StartPlanExecutionResult(toResponse(it), false) }
        require(now.toLocalTime() <= LAST_START_TIME) { "A routine can only be started until 23:50 in the gym timezone" }

        val assignment = UserPlans.innerJoin(Plans).selectAll().where {
            (UserPlans.id eq userPlanId) and (UserPlans.user eq userId) and (Plans.gym eq gymId)
        }.singleOrNull() ?: throw DomainNotFoundException("Assigned plan not found")
        val planId = assignment[Plans.id].value
        val day = Days.innerJoin(Weeks).selectAll().where {
            (Days.id eq planDayId) and (Weeks.plan eq planId)
        }.singleOrNull() ?: throw IllegalArgumentException("The plan day does not belong to the assigned plan")

        val snapshotRows = Blocks.innerJoin(BlockExercises).innerJoin(Exercises).leftJoin(QuantityTypes).selectAll().where {
            Blocks.day eq day[Days.id]
        }.orderBy(Blocks.position to SortOrder.ASC, BlockExercises.position to SortOrder.ASC).toList()
        require(snapshotRows.isNotEmpty()) { "The selected plan day has no exercises" }

        val timestamp = now.toOffsetDateTime()
        val executionId = PlanExecutions.insert {
            it[userPlan] = EntityID(userPlanId, UserPlans)
            it[planDay] = EntityID(planDayId, Days)
            it[executionDate] = today
            it[status] = STATUS_IN_PROGRESS
            it[startedAt] = timestamp
            it[lastProgressAt] = timestamp
            it[completedAt] = null
            it[totalItems] = snapshotRows.size
            it[completedItems] = 0
            it[progressPercent] = 0
        } get PlanExecutions.id
        snapshotRows.forEach { row ->
            PlanExecutionItems.insert {
                it[execution] = executionId
                it[sourceBlockExercise] = row[BlockExercises.id]
                it[blockPosition] = row[Blocks.position]
                it[blockName] = row[Blocks.name]
                it[blockLaps] = row[Blocks.laps]
                it[exercisePosition] = row[BlockExercises.position]
                it[exercise] = row[Exercises.id]
                it[exerciseName] = row[Exercises.name]
                it[quantityType] = row[BlockExercises.quantityType]
                it[repetitions] = row[BlockExercises.repetitions]
                it[quantity] = row[BlockExercises.quantity]
                it[isCompleted] = false
                it[completedAt] = null
            }
        }
        StartPlanExecutionResult(toResponse(executionRow(gymId, executionId.value)), true)
    }

    override fun get(gymId: Long, executionId: Long) = transaction(Database.connection) {
        finalizeExpired(gymId, ZonedDateTime.now(gymZone(gymId)))
        executionRowOrNull(gymId, executionId)?.let(::toResponse)
    }

    override fun updateItem(gymId: Long, executionId: Long, itemId: Long, completed: Boolean) = transaction(Database.connection) {
        val now = ZonedDateTime.now(gymZone(gymId))
        finalizeExpired(gymId, now)
        val execution = executionRow(gymId, executionId)
        requireExecutionOpen(execution, now)
        val item = PlanExecutionItems.selectAll().where {
            (PlanExecutionItems.id eq itemId) and (PlanExecutionItems.execution eq executionId)
        }.singleOrNull() ?: throw DomainNotFoundException("Execution item not found")
        val timestamp = now.toOffsetDateTime()
        PlanExecutionItems.update({ PlanExecutionItems.id eq item[PlanExecutionItems.id] }) {
            it[isCompleted] = completed
            it[completedAt] = if (completed) timestamp else null
        }
        refreshProgress(gymId, executionId, timestamp)
        toResponse(executionRow(gymId, executionId))
    }

    override fun list(gymId: Long, userId: Long, from: LocalDate, to: LocalDate) = transaction(Database.connection) {
        validateRange(from, to)
        finalizeExpired(gymId, ZonedDateTime.now(gymZone(gymId)))
        executionRows(gymId).filter { row ->
            row[UserPlans.user].value == userId &&
                !row[PlanExecutions.executionDate].isBefore(from) && !row[PlanExecutions.executionDate].isAfter(to)
        }.map(::toResponse)
    }

    override fun summaries(gymId: Long, from: LocalDate, to: LocalDate) = transaction(Database.connection) {
        validateRange(from, to)
        finalizeExpired(gymId, ZonedDateTime.now(gymZone(gymId)))
        executionRows(gymId)
            .filter { !it[PlanExecutions.executionDate].isBefore(from) && !it[PlanExecutions.executionDate].isAfter(to) }
            .groupBy { it[UserPlans.user].value }
            .map { (userId, rows) ->
                PlanExecutionSummaryResponse(
                    userId = userId,
                    completedExecutions = rows.count { it[PlanExecutions.status] == STATUS_COMPLETED },
                    partialExecutions = rows.count { it[PlanExecutions.status] == STATUS_PARTIAL },
                    inProgressExecutions = rows.count { it[PlanExecutions.status] == STATUS_IN_PROGRESS },
                    averageProgressPercent = rows.map { it[PlanExecutions.progressPercent] }.average().roundToInt()
                )
            }.sortedWith(compareByDescending<PlanExecutionSummaryResponse> { it.completedExecutions }
                .thenByDescending { it.averageProgressPercent }
                .thenBy { it.userId })
    }

    private fun refreshProgress(gymId: Long, executionId: Long, timestamp: OffsetDateTime) {
        val execution = executionRow(gymId, executionId)
        val completed = PlanExecutionItems.selectAll().where {
            (PlanExecutionItems.execution eq executionId) and (PlanExecutionItems.isCompleted eq true)
        }.count().toInt()
        val total = execution[PlanExecutions.totalItems]
        val isComplete = completed == total
        PlanExecutions.update({ PlanExecutions.id eq executionId }) {
            it[completedItems] = completed
            it[progressPercent] = ((completed * 100.0) / total).roundToInt()
            it[status] = if (isComplete) STATUS_COMPLETED else STATUS_IN_PROGRESS
            it[lastProgressAt] = timestamp
            it[completedAt] = if (isComplete) timestamp else null
        }
    }

    private fun finalizeExpired(gymId: Long, now: ZonedDateTime) {
        val today = now.toLocalDate()
        val mustCloseToday = now.toLocalTime() > LAST_START_TIME
        executionRows(gymId).filter { row ->
            row[PlanExecutions.status] == STATUS_IN_PROGRESS &&
                (row[PlanExecutions.executionDate].isBefore(today) || (mustCloseToday && row[PlanExecutions.executionDate] == today))
        }.forEach { row ->
            PlanExecutions.update({ PlanExecutions.id eq row[PlanExecutions.id] }) {
                it[status] = STATUS_PARTIAL
                it[lastProgressAt] = now.toOffsetDateTime()
            }
        }
    }

    private fun requireExecutionOpen(execution: ResultRow, now: ZonedDateTime) {
        require(execution[PlanExecutions.executionDate] == now.toLocalDate() && now.toLocalTime() <= LAST_START_TIME) {
            "This routine can no longer be updated"
        }
        require(execution[PlanExecutions.status] == STATUS_IN_PROGRESS) { "This routine is no longer in progress" }
    }

    private fun toResponse(row: ResultRow): PlanExecutionResponse {
        val executionId = row[PlanExecutions.id].value
        val items = PlanExecutionItems.selectAll().where { PlanExecutionItems.execution eq executionId }
            .orderBy(PlanExecutionItems.blockPosition to SortOrder.ASC, PlanExecutionItems.exercisePosition to SortOrder.ASC)
            .map { item ->
                PlanExecutionItemResponse(
                    id = item[PlanExecutionItems.id].value,
                    sourceBlockExerciseId = item[PlanExecutionItems.sourceBlockExercise]?.value,
                    blockPosition = item[PlanExecutionItems.blockPosition], blockName = item[PlanExecutionItems.blockName], blockLaps = item[PlanExecutionItems.blockLaps],
                    exercisePosition = item[PlanExecutionItems.exercisePosition], exerciseId = item[PlanExecutionItems.exercise]?.value, exerciseName = item[PlanExecutionItems.exerciseName],
                    quantityTypeId = item[PlanExecutionItems.quantityType]?.value, repetitions = item[PlanExecutionItems.repetitions], quantity = item[PlanExecutionItems.quantity]?.toDouble(),
                    isCompleted = item[PlanExecutionItems.isCompleted], completedAt = item[PlanExecutionItems.completedAt]?.toString()
                )
            }
        return PlanExecutionResponse(
            id = executionId, userPlanId = row[PlanExecutions.userPlan].value, planId = row[UserPlans.plan].value, planDayId = row[PlanExecutions.planDay].value,
            executionDate = row[PlanExecutions.executionDate].toString(), status = row[PlanExecutions.status], startedAt = row[PlanExecutions.startedAt].toString(),
            lastProgressAt = row[PlanExecutions.lastProgressAt].toString(), completedAt = row[PlanExecutions.completedAt]?.toString(), totalItems = row[PlanExecutions.totalItems],
            completedItems = row[PlanExecutions.completedItems], progressPercent = row[PlanExecutions.progressPercent], items = items
        )
    }

    private fun existingExecution(gymId: Long, userPlanId: Long, date: LocalDate) = executionRows(gymId).singleOrNull {
        it[PlanExecutions.userPlan].value == userPlanId && it[PlanExecutions.executionDate] == date
    }

    private fun executionRows(gymId: Long) = PlanExecutions.innerJoin(UserPlans).innerJoin(Plans).selectAll()
        .where { Plans.gym eq gymId }.toList()
    private fun executionRowOrNull(gymId: Long, id: Long) = executionRows(gymId).singleOrNull { it[PlanExecutions.id].value == id }
    private fun executionRow(gymId: Long, id: Long) = executionRowOrNull(gymId, id) ?: throw DomainNotFoundException("Plan execution not found")
    private fun gymZone(gymId: Long): ZoneId {
        val timezone = Gyms.selectAll().where { Gyms.id eq gymId }.singleOrNull()?.get(Gyms.timezone) ?: throw DomainNotFoundException("Gym not found")
        return runCatching { ZoneId.of(timezone) }.getOrElse { throw IllegalStateException("Gym has an invalid timezone") }
    }
    private fun validateRange(from: LocalDate, to: LocalDate) { require(!to.isBefore(from) && !to.isAfter(from.plusDays(366))) { "Date range must be between 0 and 366 days" } }

    private companion object {
        val LAST_START_TIME: LocalTime = LocalTime.of(23, 50)
        const val STATUS_IN_PROGRESS = "IN_PROGRESS"
        const val STATUS_PARTIAL = "PARTIAL"
        const val STATUS_COMPLETED = "COMPLETED"
    }
}
