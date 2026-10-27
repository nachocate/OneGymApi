package com.concatstudio.onegym

import com.concatstudio.onegym.model.*
import kotlin.test.Test
import kotlin.test.assertFailsWith

class PlanificationValidationTest {
    @Test
    fun `accepts a complete planning tree`() {
        PlanificationValidation.validateShape(validRequest())
    }

    @Test
    fun `rejects duplicate block positions`() {
        val request = validRequest().copy(
            weeks = listOf(
                validRequest().weeks.single().copy(
                    days = listOf(
                        validRequest().weeks.single().days.single().copy(
                            blocks = listOf(
                                validRequest().weeks.single().days.single().blocks.single(),
                                validRequest().weeks.single().days.single().blocks.single().copy(name = "Otro bloque")
                            )
                        )
                    )
                )
            )
        )

        assertFailsWith<IllegalArgumentException> { PlanificationValidation.validateShape(request) }
    }

    @Test
    fun `rejects an exercise without a measurement`() {
        val block = validRequest().weeks.single().days.single().blocks.single()
        val request = validRequest().copy(
            weeks = listOf(
                validRequest().weeks.single().copy(
                    days = listOf(validRequest().weeks.single().days.single().copy(
                        blocks = listOf(block.copy(exercises = listOf(block.exercises.single().copy(repetitions = null, quantity = null))))
                    ))
                )
            )
        )

        assertFailsWith<IllegalArgumentException> { PlanificationValidation.validateShape(request) }
    }

    private fun validRequest() = SavePlanificationRequest(
        name = "Fuerza inicial",
        planTypeId = 1,
        weeks = listOf(
            SavePlanificationWeekRequest(
                number = 1,
                days = listOf(
                    SavePlanificationDayRequest(
                        number = 1,
                        blocks = listOf(
                            SavePlanificationBlockRequest(
                                position = 1,
                                name = "Principal",
                                laps = 3,
                                exercises = listOf(
                                    SavePlanificationBlockExerciseRequest(
                                        exerciseId = 1,
                                        position = 1,
                                        repetitions = 10
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    )
}
