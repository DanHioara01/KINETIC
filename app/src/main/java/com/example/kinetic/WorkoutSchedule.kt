package com.example.kinetic

import java.util.Calendar

object WorkoutSchedule {

    data class DayPlan(
        val dayOfWeek: Int,
        val dayName: String,
        val isRestDay: Boolean,
        val muscleGroups: List<String>,
        val exercises: List<ExerciseRecommendation>,
        val focusLabel: String = "",
        val isDeloadDay: Boolean = false
    )

    private val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    fun generateWeeklySchedule(profile: UserOnboardingProfile, isDeloadActive: Boolean = false, deloadReductionFactor: Float = 0.65f): List<DayPlan> {
        val sessionsPerWeek = profile.sessionsPerWeek.coerceIn(1, 6)
        val selectedGroups = profile.selectedGroups.ifEmpty { listOf("chest", "back", "legs") }

        val workoutDays = if (profile.selectedDays.isNotEmpty()) {
            profile.selectedDays.map { dayKeyToIndex(it) }.sorted()
        } else {
            assignWorkoutDays(sessionsPerWeek)
        }
        val groupAssignments = assignGroupsToDays(workoutDays, selectedGroups)

        return dayNames.mapIndexed { index, name ->
            val dayIndex = (index + 1) % 7
            val isWorkoutDay = workoutDays.contains(dayIndex)
            val groups = if (isWorkoutDay) groupAssignments[dayIndex] ?: emptyList() else emptyList()
            val exercises = if (isWorkoutDay && groups.isNotEmpty()) {
                val base = FitnessAssistant.generateWorkoutForGroups(groups, profile.goal, profile.equipment, profile.experience, gender = profile.gender)
                if (isDeloadActive) applyDeloadToExercises(base, deloadReductionFactor) else base
            } else emptyList()

            DayPlan(
                dayOfWeek = dayIndex,
                dayName = name,
                isRestDay = !isWorkoutDay,
                muscleGroups = groups,
                exercises = exercises,
                focusLabel = if (isWorkoutDay) groups.joinToString(" + ") { it.replaceFirstChar { c -> c.uppercase() } } else "Rest Day",
                isDeloadDay = isDeloadActive && isWorkoutDay
            )
        }
    }

    fun getTodaysWorkoutOnly(profile: UserOnboardingProfile, isDeloadActive: Boolean = false, deloadReductionFactor: Float = 0.65f): DayPlan {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val dayIndex = (today + 5) % 7
        
        val workoutDays = if (profile.selectedDays.isNotEmpty()) {
            profile.selectedDays.map { dayKeyToIndex(it) }.sorted()
        } else {
            assignWorkoutDays(profile.sessionsPerWeek)
        }
        val groupsForToday = if (dayIndex in workoutDays) {
            assignGroupsToDays(listOf(dayIndex), profile.selectedGroups.ifEmpty { listOf("chest", "back", "legs") })[dayIndex] ?: emptyList()
        } else emptyList()
        
        val exercises = if (groupsForToday.isNotEmpty()) {
            val baseExercises = FitnessAssistant.generateWorkoutForGroups(groupsForToday, profile.goal, profile.equipment, profile.experience, gender = profile.gender)
            if (isDeloadActive) applyDeloadToExercises(baseExercises, deloadReductionFactor) else baseExercises
        } else emptyList()
        
        return DayPlan(
            dayOfWeek = dayIndex,
            dayName = dayNames[dayIndex],
            isRestDay = dayIndex !in workoutDays,
            muscleGroups = groupsForToday,
            exercises = exercises,
            focusLabel = if (groupsForToday.isNotEmpty()) groupsForToday.joinToString(" + ") { it.replaceFirstChar { c -> c.uppercase() } } else "Rest Day",
            isDeloadDay = isDeloadActive && groupsForToday.isNotEmpty()
        )
    }

    fun getTodayPlan(profile: UserOnboardingProfile, isDeloadActive: Boolean = false, deloadReductionFactor: Float = 0.65f): DayPlan {
        return getTodaysWorkoutOnly(profile, isDeloadActive, deloadReductionFactor)
    }

    private fun applyDeloadToExercises(exercises: List<ExerciseRecommendation>, reductionFactor: Float): List<ExerciseRecommendation> {
        val reducedCount = (exercises.size * reductionFactor).toInt().coerceAtLeast(1)
        return exercises.take(reducedCount).map { ex ->
            val reducedSets = (ex.sets * reductionFactor).toInt().coerceAtLeast(2)
            val reducedReps = reduceReps(ex.reps, reductionFactor)
            val deloadNote = if (ex.note.isNotEmpty()) "${ex.note} (Deload)" else "Deload"
            ex.copy(sets = reducedSets, reps = reducedReps, note = deloadNote)
        }
    }

    private fun reduceReps(reps: String, reductionFactor: Float): String {
        val parsed = reps.split("-").mapNotNull { it.trim().toIntOrNull() }
        if (parsed.isEmpty()) return reps
        val reduced = if (parsed.size == 2) {
            val minReps = (parsed[0] * reductionFactor).toInt().coerceAtLeast(5)
            val maxReps = (parsed[1] * reductionFactor).toInt().coerceAtLeast(6)
            "$minReps-$maxReps"
        } else {
            val r = (parsed[0] * reductionFactor).toInt().coerceAtLeast(5)
            "$r"
        }
        return reduced
    }

    private fun assignWorkoutDays(sessionsPerWeek: Int): List<Int> {
        return when (sessionsPerWeek) {
            1 -> listOf(0)
            2 -> listOf(0, 3)
            3 -> listOf(0, 2, 4)
            4 -> listOf(0, 1, 3, 4)
            5 -> listOf(0, 1, 2, 3, 4)
            6 -> listOf(0, 1, 2, 3, 4, 5)
            else -> listOf(0, 2, 4)
        }
    }

    private fun dayKeyToIndex(key: String): Int = when (key) {
        "mon" -> 0
        "tue" -> 1
        "wed" -> 2
        "thu" -> 3
        "fri" -> 4
        "sat" -> 5
        "sun" -> 6
        else -> 0
    }

    private fun assignGroupsToDays(workoutDays: List<Int>, groups: List<String>): Map<Int, List<String>> {
        val result = mutableMapOf<Int, List<String>>()
        if (workoutDays.isEmpty() || groups.isEmpty()) return result

        if (groups.size == 1) {
            workoutDays.forEach { day -> result[day] = groups }
            return result
        }

        val flattenedGroups = distributeGroups(workoutDays.size, groups)

        workoutDays.forEachIndexed { index, day ->
            result[day] = flattenedGroups.getOrNull(index) ?: emptyList()
        }

        return result
    }

    private fun distributeGroups(totalDays: Int, groups: List<String>): List<List<String>> {
        if (totalDays >= groups.size) {
            return groups.map { listOf(it) }
        }

        val result = MutableList(totalDays) { mutableListOf<String>() }
        val queue = ArrayDeque(groups)

        var dayIndex = 0
        while (queue.isNotEmpty()) {
            result[dayIndex % totalDays].add(queue.removeFirst())
            dayIndex++
        }

        return result
    }
}
