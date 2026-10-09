package com.concatstudio.onegym.mappers

import com.concatstudio.onegym.dao.*
import com.concatstudio.onegym.model.*

fun toModel(user: UserDao) = User(
    id = user.id.value,
    firstName = user.firstName,
    lastName = user.lastName,
    email = user.email,
    password = user.password,
    avatarUrl = user.avatarUrl,
    phone = user.phone,
    address = user.address
)

fun User.toResponse() = UserResponse(
    id = id,
    firstName = firstName,
    lastName = lastName,
    email = email,
    avatarUrl = avatarUrl,
    phone = phone,
    address = address
)

fun toModel(gym: GymDao) = Gym(
    id = gym.id.value,
    name = gym.name,
    description = gym.description,
    schedule = gym.schedule,
    address = gym.address,
    phone = gym.phone,
    logoUrl = gym.logoUrl,
    bannerUrl = gym.bannerUrl
)

fun toModel(role: UserRoleDao) = UserRole(role.id.value, role.name)

fun toModel(userGym: UserGymDao) = UserGym(
    id = userGym.id.value,
    userId = userGym.userId.value,
    gymId = userGym.gymId.value,
    roleId = userGym.roleId.value,
    startDate = userGym.startDate.toString(),
    endDate = userGym.endDate?.toString()
)

fun toModel(gymCoach: GymCoachDao) = GymCoach(
    id = gymCoach.id.value,
    userId = gymCoach.userId.value,
    gymId = gymCoach.gymId.value,
    coachTypeId = gymCoach.coachTypeId.value,
    startDate = gymCoach.startDate.toString(),
    endDate = gymCoach.endDate?.toString()
)

fun toModel(news: NewsDao) = News(
    id = news.id.value,
    gymId = news.gymId.value,
    title = news.title,
    description = news.description,
    date = news.date.toString(),
    imageUrl = news.imageUrl
)

fun toModel(planType: PlanTypeDao) = PlanType(planType.id.value, planType.name)

fun toModel(plan: PlanDao) = Plan(
    id = plan.id.value,
    name = plan.name,
    gymId = plan.gymId.value,
    planTypeId = plan.planTypeId.value,
    planRootId = plan.planRootId?.value
)

fun toModel(week: WeekDao) = Week(
    id = week.id.value,
    number = week.number,
    planId = week.planId.value
)

fun toModel(day: DayDao) = Day(
    id = day.id.value,
    number = day.number,
    weekId = day.weekId.value
)

fun toModel(exercise: ExerciseDao) = Exercise(
    id = exercise.id.value,
    name = exercise.name,
    description = exercise.description,
    videoUrl = exercise.videoUrl,
    imageUrl = exercise.imageUrl,
    gymId = exercise.gymId?.value,
    isVisibleGlobal = exercise.isVisibleGlobal
)

fun toModel(evaluation: EvaluationDao) = Evaluation(
    id = evaluation.id.value,
    name = evaluation.name,
    description = evaluation.description,
    creationDate = evaluation.creationDate.toString()
)

fun toModel(quantityType: QuantityTypeDao) = QuantityType(
    id = quantityType.id.value,
    name = quantityType.name
)

fun toModel(block: BlockDao) = Block(
    id = block.id.value,
    position = block.position,
    name = block.name,
    dayId = block.dayId.value,
    laps = block.laps
)

fun toModel(blockExercise: BlockExerciseDao) = BlockExercise(
    id = blockExercise.id.value,
    blockId = blockExercise.blockId.value,
    exerciseId = blockExercise.exerciseId.value,
    quantityTypeId = blockExercise.quantityTypeId?.value,
    position = blockExercise.position,
    repetitions = blockExercise.repetitions,
    quantity = blockExercise.quantity?.toDouble()
)

fun toModel(userPlan: UserPlanDao) = UserPlan(
    id = userPlan.id.value,
    userId = userPlan.userId.value,
    planId = userPlan.planId.value
)

fun toModel(activity: CurrentPlanActivityDao) = CurrentPlanActivity(
    id = activity.id.value,
    date = activity.date.toString(),
    userPlanId = activity.userPlanId.value,
    activeWeekId = activity.activeWeekId?.value,
    activeDayId = activity.activeDayId?.value,
    isActive = activity.isActive
)

fun toModel(register: RegisterDao) = Register(
    id = register.id.value,
    userPlanId = register.userPlanId.value,
    exerciseId = register.exerciseId.value,
    blockId = register.blockId.value,
    weight = register.weight.toDouble()
)

fun toModel(test: UserTestDao) = UserTest(
    id = test.id.value,
    exerciseId = test.exerciseId.value,
    userId = test.userId.value,
    evaluationId = test.evaluationId.value,
    quantityTypeId = test.quantityTypeId?.value,
    repetitions = test.repetitions,
    quantity = test.quantity?.toDouble(),
    date = test.date.toString(),
    description = test.description
)
