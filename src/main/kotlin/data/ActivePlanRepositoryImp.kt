package com.concatstudio.onegym.data

import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.model.*
import com.concatstudio.onegym.respository.ActivePlanRepository
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class ActivePlanRepositoryImp : ActivePlanRepository {
    override fun getActivePlans(userId: Long, gymId: Long): List<ActivePlanResponse> = transaction(Database.connection) {
        val activityRows = CurrentPlanActivities
            .innerJoin(UserPlans)
            .innerJoin(Plans)
            .selectAll()
            .where {
                (UserPlans.user eq userId) and
                    (Plans.gym eq gymId) and
                    (CurrentPlanActivities.isActive eq true)
            }

        if (activityRows.empty()) return@transaction emptyList()

        val planStates = linkedMapOf<Long, PlanState>()
        activityRows.forEach { row ->
            val planId = row[Plans.id].value
            val state = planStates.getOrPut(planId) {
                PlanState(name = row[Plans.name])
            }
            row[CurrentPlanActivities.activeWeek]?.value?.let(state.activeWeekIds::add)
            row[CurrentPlanActivities.activeDay]?.value?.let(state.activeDayIds::add)
        }

        val planIds = planStates.keys.toList()
        val weeks = Weeks.selectAll().where { Weeks.plan inList planIds }
            .map { row -> WeekData(row[Weeks.id].value, row[Weeks.plan].value, row[Weeks.number]) }
        val days = if (weeks.isEmpty()) emptyList() else Days.selectAll()
            .where { Days.week inList weeks.map(WeekData::id) }
            .map { row -> DayData(row[Days.id].value, row[Days.week].value, row[Days.number]) }
        val blocks = if (days.isEmpty()) emptyList() else Blocks.selectAll()
            .where { Blocks.day inList days.map(DayData::id) }
            .map { row -> BlockData(row[Blocks.id].value, row[Blocks.day].value, row[Blocks.position], row[Blocks.name], row[Blocks.laps]) }
        val exercises = if (blocks.isEmpty()) emptyList() else BlockExercises
            .innerJoin(Exercises)
            .leftJoin(QuantityTypes)
            .selectAll()
            .where { BlockExercises.block inList blocks.map(BlockData::id) }
            .map { row ->
                ExerciseData(
                    id = row[BlockExercises.id].value,
                    blockId = row[BlockExercises.block].value,
                    exerciseId = row[Exercises.id].value,
                    name = row[Exercises.name],
                    repetitions = row[BlockExercises.repetitions],
                    quantity = row[BlockExercises.quantity]?.toDouble(),
                    quantityType = row[BlockExercises.quantityType]?.value?.let {
                        QuantityType(id = it, name = row[QuantityTypes.name])
                    },
                    position = row[BlockExercises.position]
                )
            }

        val exercisesByBlock = exercises.groupBy(ExerciseData::blockId)
        val blocksByDay = blocks.groupBy(BlockData::dayId)
        val daysByWeek = days.groupBy(DayData::weekId)
        val weeksByPlan = weeks.groupBy(WeekData::planId)

        planStates.map { (planId, state) ->
            ActivePlanResponse(
                id = planId,
                name = state.name,
                isActive = true,
                weeks = weeksByPlan[planId].orEmpty().sortedBy(WeekData::number).map { week ->
                    ActivePlanWeekResponse(
                        id = week.id,
                        number = week.number,
                        isActive = week.id in state.activeWeekIds || daysByWeek[week.id].orEmpty().any { it.id in state.activeDayIds },
                        days = daysByWeek[week.id].orEmpty().sortedBy(DayData::number).map { day ->
                            ActivePlanDayResponse(
                                id = day.id,
                                number = day.number,
                                isActive = day.id in state.activeDayIds,
                                blocks = blocksByDay[day.id].orEmpty().sortedBy(BlockData::position).map { block ->
                                    ActivePlanBlockResponse(
                                        id = block.id,
                                        position = block.position,
                                        name = block.name,
                                        laps = block.laps,
                                        exercises = exercisesByBlock[block.id].orEmpty().sortedBy(ExerciseData::position).map { exercise ->
                                            ActivePlanExerciseResponse(
                                                id = exercise.id,
                                                exerciseId = exercise.exerciseId,
                                                name = exercise.name,
                                                repetitions = exercise.repetitions,
                                                quantity = exercise.quantity,
                                                quantityType = exercise.quantityType,
                                                position = exercise.position
                                            )
                                        }
                                    )
                                }
                            )
                        }
                    )
                }
            )
        }
    }

    private data class PlanState(
        val name: String,
        val activeWeekIds: MutableSet<Long> = mutableSetOf(),
        val activeDayIds: MutableSet<Long> = mutableSetOf()
    )

    private data class WeekData(val id: Long, val planId: Long, val number: Int)
    private data class DayData(val id: Long, val weekId: Long, val number: Int)
    private data class BlockData(val id: Long, val dayId: Long, val position: Int, val name: String?, val laps: Int)
    private data class ExerciseData(
        val id: Long,
        val blockId: Long,
        val exerciseId: Long,
        val name: String,
        val repetitions: Int?,
        val quantity: Double?,
        val quantityType: QuantityType?,
        val position: Int
    )
}
