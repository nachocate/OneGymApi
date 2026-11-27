package com.concatstudio.onegym.data

import com.concatstudio.onegym.dao.*
import com.concatstudio.onegym.database.Database
import com.concatstudio.onegym.model.*
import com.concatstudio.onegym.respository.PlanificationRepository
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Persists a complete routine in one database transaction. A failed validation,
 * a missing reference or a database constraint failure rolls back the full tree.
 */
class PlanificationRepositoryImp : PlanificationRepository {
    override fun getStructure(gymId: Long, planId: Long): PlanificationResponse? = transaction(Database.connection) {
        val plan = PlanDao.findById(planId) ?: return@transaction null
        if (plan.gymId.value != gymId) return@transaction null
        plan.toStructure()
    }

    override fun create(gymId: Long, request: SavePlanificationRequest): PlanificationResponse = transaction(Database.connection) {
        validateRequest(gymId, request, mustNotContainIds = true)
        val plan = PlanDao.new {
            name = request.name.trim()
            this.gymId = gymRef(gymId)
            planTypeId = planTypeRef(request.planTypeId)
            planRootId = request.planRootId?.let(::planRef)
        }
        syncWeeks(plan, request.weeks)
        plan.toStructure()
    }

    override fun replace(gymId: Long, planId: Long, request: SavePlanificationRequest): PlanificationResponse = transaction(Database.connection) {
        val plan = planInGym(gymId, planId)
        val expectedRevision = requireNotNull(request.revision) { "revision is required when updating a plan" }
        if (plan.revision != expectedRevision) {
            throw DomainConflictException("The plan was updated by another user. Reload it before saving again")
        }
        validateRequest(gymId, request, mustNotContainIds = false)
        plan.name = request.name.trim()
        plan.planTypeId = planTypeRef(request.planTypeId)
        plan.planRootId = request.planRootId?.let(::planRef)
        syncWeeks(plan, request.weeks)
        plan.revision += 1
        plan.toStructure()
    }

    override fun clone(gymId: Long, planId: Long, request: ClonePlanificationRequest): PlanificationResponse = transaction(Database.connection) {
        val source = planInGym(gymId, planId)
        val sourceRequest = source.toSaveRequest(
            name = request.name?.trim()?.takeIf(String::isNotBlank) ?: "${source.name} (copia)",
            planRootId = source.id.value
        )
        validateRequest(gymId, sourceRequest, mustNotContainIds = true)
        val copy = PlanDao.new {
            name = sourceRequest.name
            this.gymId = gymRef(gymId)
            planTypeId = planTypeRef(sourceRequest.planTypeId)
            planRootId = planRef(source.id.value)
        }
        syncWeeks(copy, sourceRequest.weeks)
        copy.toStructure()
    }

    override fun assign(gymId: Long, planId: Long, request: AssignPlanificationRequest): UserPlan = transaction(Database.connection) {
        planInGym(gymId, planId)
        if (UserDao.findById(request.userId) == null) throw DomainNotFoundException("User not found")
        if (!hasActiveMembership(request.userId, gymId)) {
            throw DomainConflictException("The user does not have an active membership in this gym")
        }
        if (UserPlanDao.find { (UserPlans.user eq request.userId) and (UserPlans.plan eq planId) }.any()) {
            throw DomainConflictException("This plan is already assigned to the user")
        }
        UserPlanDao.new {
            userId = userRef(request.userId)
            this.planId = planRef(planId)
        }.toUserPlan()
    }

    override fun getMemberPlans(gymId: Long, userId: Long): List<UserPlan> = transaction(Database.connection) {
        if (UserDao.findById(userId) == null) throw DomainNotFoundException("User not found")
        if (!hasActiveMembership(userId, gymId)) return@transaction emptyList()
        UserPlanDao.find { UserPlans.user eq userId }
            .filter { it.planId.value.let(PlanDao::findById)?.gymId?.value == gymId }
            .map { it.toUserPlan() }
    }

    private fun planInGym(gymId: Long, planId: Long): PlanDao {
        val plan = PlanDao.findById(planId) ?: throw DomainNotFoundException("Plan not found")
        if (plan.gymId.value != gymId) throw DomainNotFoundException("Plan not found in this gym")
        return plan
    }

    private fun validateRequest(gymId: Long, request: SavePlanificationRequest, mustNotContainIds: Boolean) {
        if (GymDao.findById(gymId) == null) throw DomainNotFoundException("Gym not found")
        PlanificationValidation.validateShape(request)
        if (PlanTypeDao.findById(request.planTypeId) == null) throw DomainNotFoundException("Plan type not found")
        request.planRootId?.let { rootId ->
            val root = PlanDao.findById(rootId) ?: throw DomainNotFoundException("Plan root not found")
            require(root.gymId.value == gymId) { "Plan root must belong to the same gym" }
        }
        request.weeks.forEach { week ->
            week.days.forEach { day ->
                day.blocks.forEach { block ->
                    block.exercises.forEach { exercise ->
                        val exerciseDao = ExerciseDao.findById(exercise.exerciseId)
                            ?: throw DomainNotFoundException("Exercise not found")
                        val exerciseGymId = exerciseDao.gymId?.value
                        require(exerciseGymId == null || exerciseGymId == gymId) {
                            "Exercise is not available to this gym"
                        }
                        exercise.quantityTypeId?.let { quantityTypeId ->
                            if (QuantityTypeDao.findById(quantityTypeId) == null) {
                                throw DomainNotFoundException("Quantity type not found")
                            }
                        }
                    }
                }
            }
        }
        if (mustNotContainIds) requireNoIds(request)
    }

    private fun requireNoIds(request: SavePlanificationRequest) {
        request.weeks.forEach { week ->
            require(week.id == null) { "IDs are not allowed when creating a planification" }
            week.days.forEach { day ->
                require(day.id == null) { "IDs are not allowed when creating a planification" }
                day.blocks.forEach { block ->
                    require(block.id == null) { "IDs are not allowed when creating a planification" }
                    block.exercises.forEach { exercise ->
                        require(exercise.id == null) { "IDs are not allowed when creating a planification" }
                    }
                }
            }
        }
    }

    private fun syncWeeks(plan: PlanDao, requests: List<SavePlanificationWeekRequest>) {
        val existing = WeekDao.find { Weeks.plan eq plan.id }.associateBy { it.id.value }
        requireUniqueIds(requests.mapNotNull { it.id }, "Week")
        val requestedIds = requests.mapNotNull { it.id }.toSet()
        existing.filterKeys { it !in requestedIds }.values.forEach(::deleteWeekTree)
        relabelWeeks(existing.filterKeys { it in requestedIds }.values.toList())

        requests.forEach { request ->
            val week = request.id?.let { existing[it] ?: throw IllegalArgumentException("Week does not belong to this plan") }
                ?: WeekDao.new { number = request.number; planId = plan.id }
            week.number = request.number
            syncDays(week, request.days)
        }
    }

    private fun syncDays(week: WeekDao, requests: List<SavePlanificationDayRequest>) {
        val existing = DayDao.find { Days.week eq week.id }.associateBy { it.id.value }
        requireUniqueIds(requests.mapNotNull { it.id }, "Day")
        val requestedIds = requests.mapNotNull { it.id }.toSet()
        existing.filterKeys { it !in requestedIds }.values.forEach(::deleteDayTree)
        relabelDays(existing.filterKeys { it in requestedIds }.values.toList())

        requests.forEach { request ->
            val day = request.id?.let { existing[it] ?: throw IllegalArgumentException("Day does not belong to this week") }
                ?: DayDao.new { number = request.number; weekId = week.id }
            day.number = request.number
            syncBlocks(day, request.blocks)
        }
    }

    private fun syncBlocks(day: DayDao, requests: List<SavePlanificationBlockRequest>) {
        val existing = BlockDao.find { Blocks.day eq day.id }.associateBy { it.id.value }
        requireUniqueIds(requests.mapNotNull { it.id }, "Block")
        val requestedIds = requests.mapNotNull { it.id }.toSet()
        existing.filterKeys { it !in requestedIds }.values.forEach(::deleteBlockTree)
        relabelBlocks(existing.filterKeys { it in requestedIds }.values.toList())

        requests.forEach { request ->
            val block = request.id?.let { existing[it] ?: throw IllegalArgumentException("Block does not belong to this day") }
                ?: BlockDao.new { position = request.position; dayId = day.id; laps = request.laps }
            block.position = request.position
            block.name = request.name
            block.laps = request.laps
            syncBlockExercises(block, request.exercises)
        }
    }

    private fun syncBlockExercises(block: BlockDao, requests: List<SavePlanificationBlockExerciseRequest>) {
        val existing = BlockExerciseDao.find { BlockExercises.block eq block.id }.associateBy { it.id.value }
        requireUniqueIds(requests.mapNotNull { it.id }, "Block exercise")
        val requestedIds = requests.mapNotNull { it.id }.toSet()
        existing.filterKeys { it !in requestedIds }.values.forEach { it.delete() }
        relabelBlockExercises(existing.filterKeys { it in requestedIds }.values.toList())

        requests.forEach { request ->
            val item = request.id?.let { existing[it] ?: throw IllegalArgumentException("Exercise does not belong to this block") }
                ?: BlockExerciseDao.new { blockId = block.id; position = request.position; exerciseId = exerciseRef(request.exerciseId) }
            item.blockId = block.id
            item.exerciseId = exerciseRef(request.exerciseId)
            item.quantityTypeId = request.quantityTypeId?.let(::quantityTypeRef)
            item.position = request.position
            item.repetitions = request.repetitions
            item.quantity = request.quantity?.let(BigDecimal::valueOf)
        }
    }

    private fun deleteWeekTree(week: WeekDao) {
        DayDao.find { Days.week eq week.id }.forEach(::deleteDayTree)
        week.delete()
    }

    private fun deleteDayTree(day: DayDao) {
        BlockDao.find { Blocks.day eq day.id }.forEach(::deleteBlockTree)
        day.delete()
    }

    private fun deleteBlockTree(block: BlockDao) {
        BlockExerciseDao.find { BlockExercises.block eq block.id }.forEach { it.delete() }
        block.delete()
    }

    // Temporary positive values let an editor swap positions/numbers without violating UNIQUE constraints mid-update.
    private fun relabelWeeks(items: List<WeekDao>) = items.forEachIndexed { index, item -> item.number = TEMPORARY_ORDER_BASE + index }
    private fun relabelDays(items: List<DayDao>) = items.forEachIndexed { index, item -> item.number = TEMPORARY_ORDER_BASE + index }
    private fun relabelBlocks(items: List<BlockDao>) = items.forEachIndexed { index, item -> item.position = TEMPORARY_ORDER_BASE + index }
    private fun relabelBlockExercises(items: List<BlockExerciseDao>) = items.forEachIndexed { index, item -> item.position = TEMPORARY_ORDER_BASE + index }

    private fun requireUniqueIds(ids: List<Long>, label: String) {
        require(ids.size == ids.toSet().size) { "$label IDs must not be repeated" }
    }

    private fun hasActiveMembership(userId: Long, gymId: Long): Boolean {
        val today = LocalDate.now()
        return UserGyms.selectAll().where {
            (UserGyms.user eq userId) and
                (UserGyms.gym eq gymId) and
                (UserGyms.startDate lessEq today) and
                (UserGyms.endDate.isNull() or (UserGyms.endDate greaterEq today))
        }.any()
    }

    private fun PlanDao.toStructure(): PlanificationResponse = PlanificationResponse(
        id = id.value,
        name = name,
        gymId = gymId.value,
        planTypeId = planTypeId.value,
        planRootId = planRootId?.value,
        revision = revision,
        weeks = WeekDao.find { Weeks.plan eq id }.orderBy(Weeks.number to SortOrder.ASC).map { week ->
            PlanificationWeekResponse(
                id = week.id.value,
                number = week.number,
                days = DayDao.find { Days.week eq week.id }.orderBy(Days.number to SortOrder.ASC).map { day ->
                    PlanificationDayResponse(
                        id = day.id.value,
                        number = day.number,
                        blocks = BlockDao.find { Blocks.day eq day.id }.orderBy(Blocks.position to SortOrder.ASC).map { block ->
                            PlanificationBlockResponse(
                                id = block.id.value,
                                position = block.position,
                                name = block.name,
                                laps = block.laps,
                                exercises = BlockExerciseDao.find { BlockExercises.block eq block.id }
                                    .orderBy(BlockExercises.position to SortOrder.ASC)
                                    .map { item ->
                                        PlanificationBlockExerciseResponse(
                                            id = item.id.value,
                                            exerciseId = item.exerciseId.value,
                                            quantityTypeId = item.quantityTypeId?.value,
                                            position = item.position,
                                            repetitions = item.repetitions,
                                            quantity = item.quantity?.toDouble()
                                        )
                                    }
                            )
                        }
                    )
                }
            )
        }
    )

    private fun PlanDao.toSaveRequest(name: String, planRootId: Long?): SavePlanificationRequest = SavePlanificationRequest(
        name = name,
        planTypeId = planTypeId.value,
        planRootId = planRootId,
        weeks = WeekDao.find { Weeks.plan eq id }.orderBy(Weeks.number to SortOrder.ASC).map { week ->
            SavePlanificationWeekRequest(
                number = week.number,
                days = DayDao.find { Days.week eq week.id }.orderBy(Days.number to SortOrder.ASC).map { day ->
                    SavePlanificationDayRequest(
                        number = day.number,
                        blocks = BlockDao.find { Blocks.day eq day.id }.orderBy(Blocks.position to SortOrder.ASC).map { block ->
                            SavePlanificationBlockRequest(
                                position = block.position,
                                name = block.name,
                                laps = block.laps,
                                exercises = BlockExerciseDao.find { BlockExercises.block eq block.id }
                                    .orderBy(BlockExercises.position to SortOrder.ASC)
                                    .map { item ->
                                        SavePlanificationBlockExerciseRequest(
                                            exerciseId = item.exerciseId.value,
                                            quantityTypeId = item.quantityTypeId?.value,
                                            position = item.position,
                                            repetitions = item.repetitions,
                                            quantity = item.quantity?.toDouble()
                                        )
                                    }
                            )
                        }
                    )
                }
            )
        }
    )

    private fun UserPlanDao.toUserPlan() = UserPlan(id = id.value, userId = userId.value, planId = planId.value)

    private companion object {
        const val TEMPORARY_ORDER_BASE = 2_000_000_000
    }
}
