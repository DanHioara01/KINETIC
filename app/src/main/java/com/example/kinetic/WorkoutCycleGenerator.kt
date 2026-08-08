package com.example.kinetic

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class GymDayType {
    TRAINING,
    REST
}

data class CycleDay(
    val dayNumber: Int,
    val dayType: GymDayType,
    val muscleGroups: List<String>
)

data class TodayWorkout(
    val date: LocalDate,
    val dayInCycle: Int,
    val dayType: GymDayType,
    val muscleGroups: List<String>,
    val exercises: List<ExerciseRecommendation>,
    val isDeloadActive: Boolean = false
)

object WorkoutCycleGenerator {

    private val muscleGroupPriorities = listOf(
        "chest", "triceps",
        "back", "biceps",
        "legs", "shoulders",
        "abs", "glutes", "hamstrings"
    )

    private val complementaryPairs = listOf(
        listOf("chest", "triceps"),
        listOf("back", "biceps"),
        listOf("legs", "shoulders"),
        listOf("abs", "glutes")
    )

    fun generateCycle(workoutDaysPerWeek: Int, selectedMuscleGroups: List<String>): List<CycleDay> {
        val normalized = selectedMuscleGroups.map {
            val lower = it.lowercase().trim()
            when (lower) {
                "arms" -> "biceps"
                "core" -> "abs"
                "butt" -> "glutes"
                "quads" -> "legs"
                else -> lower
            }
        }.distinct()

        if (workoutDaysPerWeek <= 0 || normalized.isEmpty()) {
            return (1..7).map { CycleDay(it, GymDayType.REST, emptyList()) }
        }

        val groupsToUse = normalized.filter { it in muscleGroupPriorities }
            .sortedBy { muscleGroupPriorities.indexOf(it) }

        val trainingDayGroups = distributeGroupsIntoDays(groupsToUse, workoutDaysPerWeek)
        val cycle = mutableListOf<CycleDay>()
        var currentDay = 1

        for (dayGroups in trainingDayGroups) {
            cycle.add(CycleDay(currentDay, GymDayType.TRAINING, dayGroups))
            currentDay++

            if (currentDay <= 7) {
                cycle.add(CycleDay(currentDay, GymDayType.REST, emptyList()))
                currentDay++
            }
        }

        while (currentDay <= 7) {
            cycle.add(CycleDay(currentDay, GymDayType.REST, emptyList()))
            currentDay++
        }

        return cycle.take(7)
    }

    private fun distributeGroupsIntoDays(
        groups: List<String>,
        numDays: Int
    ): List<List<String>> {
        if (groups.isEmpty()) return emptyList()

        val paired = mutableListOf<List<String>>()
        val remaining = groups.toMutableList()

        for (pair in complementaryPairs) {
            val matchedGroups = pair.filter { it in remaining }
            if (matchedGroups.size == 2) {
                paired.add(matchedGroups)
                remaining.removeAll(matchedGroups)
            } else if (matchedGroups.size == 1 && remaining.size > 0) {
                val single = matchedGroups.first()
                remaining.remove(single)

                val complement = pair.firstOrNull { it in remaining }
                if (complement != null) {
                    paired.add(listOf(single, complement))
                    remaining.remove(complement)
                } else {
                    val nextSolo = remaining.removeFirstOrNull()
                    if (nextSolo != null) {
                        paired.add(listOf(single, nextSolo))
                    } else {
                        paired.add(listOf(single))
                    }
                }
            }
        }

        for (group in remaining) {
            val lastDay = paired.lastOrNull()
            if (lastDay != null && lastDay.size < 3) {
                paired[paired.lastIndex] = lastDay + group
            } else {
                paired.add(listOf(group))
            }
        }

        return paired.take(numDays)
    }

    fun getDayInCycle(startDate: LocalDate, today: LocalDate): Int {
        val daysBetween = ChronoUnit.DAYS.between(startDate, today)
        val dayInCycle = (daysBetween % 7).toInt() + 1
        return if (dayInCycle < 1) dayInCycle + 7 else dayInCycle
    }

    fun getTodayType(cycle: List<CycleDay>, dayInCycle: Int): GymDayType {
        return cycle.firstOrNull { it.dayNumber == dayInCycle }?.dayType ?: GymDayType.REST
    }

    fun getTodayMuscleGroups(cycle: List<CycleDay>, dayInCycle: Int): List<String> {
        return cycle.firstOrNull { it.dayNumber == dayInCycle }?.muscleGroups ?: emptyList()
    }

    // PPL split: focus day -> muscle groups. "legs" covers quads + hamstrings (both live under Picioare).
    private val pplSplit = listOf(
        listOf("chest", "shoulders", "triceps"), // push
        listOf("back", "biceps", "forearms"),    // pull
        listOf("legs", "glutes", "calves")       // legs
    )

    /**
     * The user's training days (Mon=0 … Sun=6). Uses the days chosen in onboarding
     * (selectedDays); falls back to an even spread based on sessionsPerWeek.
     */
    fun trainingDayIndices(profile: UserOnboardingProfile): List<Int> {
        if (profile.selectedDays.isNotEmpty()) {
            return profile.selectedDays.mapNotNull { dayKeyToIndex(it) }.distinct().sorted()
        }
        return defaultTrainingDays(profile.sessionsPerWeek)
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

    private fun defaultTrainingDays(sessionsPerWeek: Int): List<Int> = when (sessionsPerWeek) {
        1 -> listOf(0)
        2 -> listOf(0, 3)
        3 -> listOf(0, 2, 4)
        4 -> listOf(0, 1, 3, 4)
        5 -> listOf(0, 1, 2, 3, 4)
        6 -> listOf(0, 1, 2, 3, 4, 5)
        else -> listOf(0, 2, 4)
    }

    // Today's weekday as Mon=0 … Sun=6 (LocalDate.dayOfWeek.value is 1=Mon … 7=Sun).
    private fun todayWeekdayIndex(): Int = (LocalDate.now().dayOfWeek.value + 6) % 7

    // Number of training sessions between startDate and today (inclusive). Drives the PPL rotation.
    private fun trainingSessionCount(startDate: LocalDate, today: LocalDate, trainingDays: List<Int>): Int {
        if (trainingDays.isEmpty()) return 0
        var count = 0
        var day = startDate
        while (!day.isAfter(today)) {
            val idx = (day.dayOfWeek.value + 6) % 7
            if (idx in trainingDays) count++
            day = day.plusDays(1)
        }
        return count
    }

    fun buildTodayWorkout(profile: UserOnboardingProfile, startDate: LocalDate, isDeloadActive: Boolean = false, deloadReductionFactor: Float = 0.65f): TodayWorkout {
        val today = LocalDate.now()
        val trainingDays = trainingDayIndices(profile)
        val isTrainingDay = todayWeekdayIndex() in trainingDays
        val dayInCycle = getDayInCycle(startDate, today)
        val dayType = if (isTrainingDay) GymDayType.TRAINING else GymDayType.REST

        // PPL rotation across the user's actual training days: 1st = push, 2nd = pull, 3rd = legs, then repeats.
        val muscleGroups = if (isTrainingDay) {
            val sessionNumber = trainingSessionCount(startDate, today, trainingDays).coerceAtLeast(1)
            pplSplit[(sessionNumber - 1) % pplSplit.size]
        } else {
            emptyList()
        }

        val exercises = if (dayType == GymDayType.TRAINING) {
            val base = FitnessAssistant.generateWorkoutForGroups(muscleGroups, profile.goal, profile.equipment, profile.experience, gender = profile.gender, dayIndex = dayInCycle)
            if (isDeloadActive) applyDeloadToExercises(base, deloadReductionFactor) else base
        } else {
            emptyList()
        }

        return TodayWorkout(
            date = today,
            dayInCycle = dayInCycle,
            dayType = dayType,
            muscleGroups = muscleGroups,
            exercises = exercises,
            isDeloadActive = isDeloadActive
        )
    }

    private fun applyDeloadToExercises(exercises: List<ExerciseRecommendation>, factor: Float): List<ExerciseRecommendation> {
        val reducedCount = (exercises.size * factor).toInt().coerceAtLeast(1)
        return exercises.take(reducedCount).map { ex ->
            val reducedSets = (ex.sets * factor).toInt().coerceAtLeast(2)
            val reducedReps = reduceReps(ex.reps, factor)
            ex.copy(
                sets = reducedSets,
                reps = reducedReps,
                note = if (ex.note.isNotEmpty()) "${ex.note} (Deload)" else "Deload"
            )
        }
    }

    private fun reduceReps(reps: String, factor: Float): String {
        val dashIdx = reps.indexOf('-')
        if (dashIdx > 0) {
            val min = reps.substring(0, dashIdx).trim().toIntOrNull() ?: return reps
            val max = reps.substring(dashIdx + 1).trim().toIntOrNull() ?: return reps
            val newMin = (min * factor).toInt().coerceAtLeast(5)
            val newMax = (max * factor).toInt().coerceAtLeast(6)
            return "$newMin-$newMax"
        }
        val single = reps.trim().toIntOrNull() ?: return reps
        return (single * factor).toInt().coerceAtLeast(5).toString()
    }

    fun formatGroupName(group: String, strings: LanguageManager.Strings? = null): String {
        val romanian = when (group) {
            "chest" -> "Piept"
            "back" -> "Spate"
            "legs" -> "Picioare"
            "shoulders" -> "Umeri"
            "biceps" -> "Biceps"
            "triceps" -> "Triceps"
            "abs" -> "Abdomen"
            "core" -> "Abdomen"
            "glutes" -> "Fese"
            "hamstrings" -> "Picioare"
            "calves" -> "Gambe"
            "forearms" -> "Antebrate"
            "arms" -> "Brațe"
            "quads" -> "Picioare"
            else -> group
        }
        if (strings != null) {
            return LanguageManager.translateMuscleGroup(romanian, strings)
        }
        return romanian.replaceFirstChar { it.uppercase() }
    }
}
