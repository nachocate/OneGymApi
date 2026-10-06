package com.concatstudio.onegym.modules

import com.concatstudio.onegym.data.*
import com.concatstudio.onegym.respository.*
import com.concatstudio.onegym.security.JwtService
import com.concatstudio.onegym.security.JwtSettings
import org.koin.dsl.module

val appDiModules = module {
    single { JwtSettings.load() }
    single { JwtService(get()) }
    single<RefreshTokenRepository> { RefreshTokenRepositoryImp() }
    single<UserStatusRepository> { UserStatusRepositoryImp() }
    single<GymHomeRepository> { GymHomeRepositoryImp() }
    single<CoachTypeRepository> { CoachTypeRepositoryImp() }
    single<GymCoachRepository> { GymCoachRepositoryImp() }
    single<UserRepository> { UserRepositoryImp() }
    single<GymRepository> { GymRepositoryImp() }
    single<UserRoleRepository> { UserRoleRepositoryImp() }
    single<UserGymRepository> { UserGymRepositoryImp() }
    single<NewsRepository> { NewsRepositoryImp() }
    single<PlanTypeRepository> { PlanTypeRepositoryImp() }
    single<PlanRepository> { PlanRepositoryImp() }
    single<WeekRepository> { WeekRepositoryImp() }
    single<DayRepository> { DayRepositoryImp() }
    single<ExerciseRepository> { ExerciseRepositoryImp() }
    single<QuantityTypeRepository> { QuantityTypeRepositoryImp() }
    single<BlockRepository> { BlockRepositoryImp() }
    single<BlockExerciseRepository> { BlockExerciseRepositoryImp() }
    single<UserPlanRepository> { UserPlanRepositoryImp() }
    single<CurrentPlanActivityRepository> { CurrentPlanActivityRepositoryImp() }
    single<RegisterRepository> { RegisterRepositoryImp() }
    single<UserTestRepository> { UserTestRepositoryImp() }
}
