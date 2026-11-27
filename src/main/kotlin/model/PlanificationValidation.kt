package com.concatstudio.onegym.model

/** Pure validation shared by the HTTP planning contract and its unit tests. */
object PlanificationValidation {
    fun validateShape(request: SavePlanificationRequest) {
        require(request.name.isNotBlank()) { "Plan name is required" }
        require(request.weeks.isNotEmpty()) { "A planification requires at least one week" }
        requireDistinct(request.weeks.map { it.number }, "Week numbers")
        request.weeks.forEach { week ->
            require(week.number > 0) { "Week number must be positive" }
            require(week.days.isNotEmpty()) { "Each week requires at least one day" }
            requireDistinct(week.days.map { it.number }, "Day numbers")
            week.days.forEach { day ->
                require(day.number > 0) { "Day number must be positive" }
                require(day.blocks.isNotEmpty()) { "Each day requires at least one block" }
                requireDistinct(day.blocks.map { it.position }, "Block positions")
                day.blocks.forEach { block ->
                    require(block.position > 0) { "Block position must be positive" }
                    require(block.laps > 0) { "Block laps must be positive" }
                    require(block.exercises.isNotEmpty()) { "Each block requires at least one exercise" }
                    requireDistinct(block.exercises.map { it.position }, "Exercise positions")
                    block.exercises.forEach { exercise ->
                        require(exercise.position > 0) { "Exercise position must be positive" }
                        require(exercise.repetitions != null || exercise.quantity != null) {
                            "Each block exercise requires repetitions or quantity"
                        }
                        require(exercise.repetitions == null || exercise.repetitions >= 0) {
                            "Repetitions cannot be negative"
                        }
                        require(exercise.quantity == null || exercise.quantity >= 0) {
                            "Quantity cannot be negative"
                        }
                    }
                }
            }
        }
    }

    private fun requireDistinct(values: List<Int>, label: String) {
        require(values.size == values.toSet().size) { "$label must be unique inside their parent" }
    }
}
